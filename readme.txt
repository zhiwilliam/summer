service test command:

curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "123456"}'

curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "123456"}'

查找：
curl -X GET http://localhost:8080/api/me \
  -H "Authorization: Bearer eyJ0eXAiOiJK... "