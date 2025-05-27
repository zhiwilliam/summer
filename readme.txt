service test command:

数据库：
docker exec -it evday-postgres psql -U postgres -d evday

curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "123456"}'

curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "123456"}'

查找：
curl -X GET http://localhost:8080/api/me \
  -H "Authorization: Bearer eyJ0eXAiOiJK... "

  定义后端接口

  例如：

  路由 /api/notes（POST）
  // 请求体
  {
    "title": "今天在东京街头",
    "content": "遇到很多有趣的人",
    "images": [
      "https://minio.evday.com/uploads/abc.jpg",
      "https://minio.evday.com/uploads/xyz.jpg"
    ]
  }
  响应体：
  {
    "id": "note-uuid",
    "createdAt": "2025-05-27T12:00:00Z"
  }
