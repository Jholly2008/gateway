# gateway
gateway


## 构建
chmod +x build.sh
bash build.sh


## 启动
docker run -p 10000:10000 -e JVM_MEM=512M -e SERVICE_A_URI=http://170.106.189.13:10001 --name gateway kkk2099/kkk:gateway-1.0