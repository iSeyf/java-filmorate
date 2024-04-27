# java-filmorate
Template repository for Filmorate project.

## ER Diagram
![Diagram](Diagram.png)

### Примеры запросов:
Вывести всех пользователей:
```
SELECT *
FROM user;
```
Вывести друзей конкретного(N) пользователя:
```
SELECT u.name AS friend_name
FROM friend f
JOIN user u ON f.friend_id = u.user_id
WHERE
    f.user_id = x //(х = id N пользователя)
    AND f.status = true;
```
