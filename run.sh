#!/bin/bash

# 显示使用方法
usage() {
    echo "Usage: $0 <version> <service_a_uri>"
    echo "Example: $0 1.0 http://170.106.189.13:10001"
    exit 1
}

# 检查参数个数
if [ $# -ne 2 ]; then
    usage
fi

# 获取参数
VERSION=$1
SERVICE_A_URI=$2

# 容器配置
CONTAINER_NAME="gateway"
IMAGE_NAME="kkk2099/kkk"
SERVICE_NAME="gateway"
PORT="10000"

# 检查并移除已存在的容器
if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
    echo "Stopping and removing existing container..."
    docker stop $CONTAINER_NAME
    docker rm $CONTAINER_NAME
fi

# 运行新容器
echo "Starting $SERVICE_NAME..."
echo "Version: $VERSION"
echo "Service A URI: $SERVICE_A_URI"

docker run -d \
    -p $PORT:$PORT \
    -e JVM_MEM=512M \
    -e SERVICE_A_URI=$SERVICE_A_URI \
    --name $CONTAINER_NAME \
    $IMAGE_NAME:$SERVICE_NAME-$VERSION
