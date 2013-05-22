export CANONICAL_HOST=www.refheap.com
export HOSTS=refheap.com,www.refheap.com
export JVM_OPTS="-Xmx60m"
export MONGOLAB_URI=mongodb://127.0.0.1/refheap
export MONGO_URI=mongodb://127.0.0.1/refheap
export PORT=43535
export LEIN_NO_DEV=t

lein trampoline ring server-headless
