package macros

import cats.effect.{Resource, Sync}
import scala.annotation.MacroAnnotation
import scala.quoted.*

class Module extends MacroAnnotation {
  def transform(using q: Quotes)(
    definition: q.reflect.Definition,
    companion: Option[q.reflect.Definition]
  ): List[q.reflect.Definition] = {
    import q.reflect.*

    report.info(s"Processing definition: ${definition.show}", definition.pos)
    definition match {
      case defDef @ DefDef(methodName, paramss, returnTpt, Some(rhs)) =>
        report.info(s"Found DefDef: $methodName, return type: ${returnTpt.tpe.show}", returnTpt.pos)
        val returnTpe = returnTpt.tpe

        // 检查返回类型是否为 Resource[F, A]
        returnTpe match {
          case AppliedType(resourceType, List(fType, aType)) if resourceType =:= TypeRepr.of[Resource] =>
            report.info(s"Return type is Resource, fType: ${fType.show}, aType: ${aType.show}", returnTpt.pos)
            // 解析 fType，确保它是 F[_]
            val isTypeConstructor = fType match {
              case TypeLambda(_, _, _) => true // 直接是 F[_]
              case TypeRef(qual, name) =>
                // 检查方法定义的类型参数 F[_]
                paramss.exists {
                  case TypeParamClause(params) =>
                    params.exists {
                      case TypeDef(paramName, rhs) =>
                        rhs match {
                          case LambdaTypeTree(_, _) => paramName == name
                          case _ => false
                        }
                      case _ => false
                    }
                  case _ => false
                }
              case _ => false
            }
            if (!isTypeConstructor) {
              report.errorAndAbort(s"返回类型的第一个参数必须是一个类型构造器 F[_], 实际为: ${fType.show}", returnTpt.pos)
            }
            report.info(s"fType is valid type constructor: ${fType.show}", returnTpt.pos)
            // 将 fType 转换为 Type，aType 直接使用
            fType.asType match {
              case '[fType] =>
                aType.asType match {
                  case '[a] =>
                    // 生成 given 实例
                    val givenDef = '{
                      given loggerModule[F[_]: Sync]: module.Module[F, a] with {
                        def make: Resource[F, a] = ${ Ref(defDef.symbol).asExprOf[Resource[fType, a]] }
                      }
                    }.asTerm.asInstanceOf[ValDef]
                    // 返回原始方法和生成的 given
                    List(defDef, givenDef)
                  case _ =>
                    report.errorAndAbort(s"无法提取类型 A: ${aType.show}", returnTpt.pos)
                }
              case _ =>
                report.errorAndAbort(s"无法提取类型 F: ${fType.show}", returnTpt.pos)
            }
          case _ =>
            report.errorAndAbort(s"方法返回类型必须是 Resource[F[_], A], 实际为: ${returnTpe.show}", returnTpt.pos)
            List(definition)
        }
      case _ =>
        report.errorAndAbort("@Module 只能应用于方法定义", definition.pos)
        List(definition)
    }
  }
}