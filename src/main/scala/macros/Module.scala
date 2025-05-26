package macros

import scala.annotation.MacroAnnotation
import scala.quoted.*
import cats.effect.Resource

// --- 2. Annotation class ---
class Module extends MacroAnnotation:
  def transform(using q: Quotes)(
    tree: quotes.reflect.Definition,
    companion: Option[quotes.reflect.Definition]
  ): List[quotes.reflect.Definition] = ModuleMacro.transform(tree)

// --- 3. Macro implementation ---
object ModuleMacro:
  def transform(using Quotes)(tree: quotes.reflect.Definition): List[quotes.reflect.Definition] =
    import quotes.reflect.*

    tree match
      case defDef @ DefDef(methodName, paramss, returnTpt, Some(rhs)) =>

        returnTpt.tpe match
          case AppliedType(resourceType, List(fType, aType)) if resourceType.typeSymbol.fullName.contains("Resource") =>

            // Extract type parameters (like F[_])
            val typeParamSyms = paramss.collectFirst {
              case TypeParamClause(params) => params.map(_.symbol)
            }.getOrElse(Nil)

            val typeParamTrees = typeParamSyms.map(TypeIdent(_))

            val moduleTypeRepr = Symbol.requiredClass("module.Module").typeRef.appliedTo(List(fType, aType))
            val moduleTrait = Inferred(moduleTypeRepr) // ✅ 正确

            val givenName = Symbol.freshName(aType.typeSymbol.name.toLowerCase + "Module")
            val givenSym = Symbol.newVal(Symbol.spliceOwner, givenName, moduleTypeRepr, Flags.Given, Symbol.noSymbol)

            val methodSym = Symbol.newMethod(
              givenSym,
              "make",
              MethodType(Nil)(_ => Nil, _ => returnTpt.tpe)
            )

            val makeDef = DefDef(methodSym, _ => Some(Ref(defDef.symbol).appliedToTypeTrees(typeParamTrees)))

            val givenVal = ValDef(
              givenSym,
              Some(Block(List(makeDef), Typed(New(moduleTrait), moduleTrait)))
            )

            List(defDef, givenVal)

          case _ =>
            report.error("Return type must be Resource[F, A]", returnTpt.pos)
            List(tree)

      case _ =>
        report.error("@Module can only be used on defs", tree.pos)
        List(tree)