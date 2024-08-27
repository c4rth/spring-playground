java org.springframework.boot.loader.launch.JarLauncher
#curl -sf -XPOST http://127.0.0.1:15020/quitquitquit
echo "Call istio /quitquitquit"
wget -q --post-data='' http://127.0.0.1:15020/quitquitquit
echo "Done"