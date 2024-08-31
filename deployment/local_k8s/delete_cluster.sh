kubectl -n kafka delete $(kubectl get strimzi -o name -n kafka)
sleep 3000
kubectl delete pvc -l strimzi.io/name=my-cluster-kafka -n kafka