# gateway
gateway


## 构建
chmod +x build.sh
bash build.sh


## 启动
docker run -p 10000:10000 -e JVM_MEM=512M --name gateway kkk2099/kkk:gateway-1.0