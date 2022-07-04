docker stop $(docker ps -aq)
docker rm $(docker ps -aq) -f
docker rmi $(docker images -q) -f
docker volume rm $(docker volume ls -q)
docker system prune -af
#docker network rm $(docker network ls -q)
