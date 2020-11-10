function LOG (){
	echo -e "    \033[0;31myuki#\033[0m $1"
}

function HEADER (){
	echo -e "=========="	
}

function FOOTER (){
	echo -e "=========="	

	unset TOOLKIT_APPLICATION
	unset TOOLKIT_TYPE
	unset TOOLKIT_VERSION
	unset TOOLKIT_ARGS
	unset TOOLKIT_PORTS
}

function .docker-delete (){
	HEADER
	## local BASE_DIR=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd -P);

	. ./toolkit.conf 

	local container_id=$(sudo docker ps -a -q -f "label=application=$TOOLKIT_APPLICATION" -f "label=type=$TOOLKIT_TYPE")

	if [[ -z "$container_id" ]]; then
		LOG "There is not container with tag $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
	else
		LOG "Stopping container: $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
		sudo docker stop $container_id

		LOG "Dropping container: $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
		sudo docker rm $container_id		
	fi

	local image_id=$(sudo docker images -f "label=application=$TOOLKIT_APPLICATION" -f "label=type=$TOOLKIT_TYPE" -q --no-trunc)

	if [[ -z "$image_id" ]]; then
		LOG "There is not image with tag $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
	else
		LOG "Deleting image for $TOOLKIT_APPLICATION:$TOOLKIT_TYPE ($image_id)"
		sudo docker rmi $(echo $image_id)
	fi

	FOOTER
}

function .docker-build (){
	HEADER
	
	. ./toolkit.conf 

	local container_id=$(sudo docker ps -a -q  -f "label=application=$TOOLKIT_APPLICATION" -f "label=type=$TOOLKIT_TYPE")

	if [[ -z "$container_id" ]]; then
		LOG "There is not container with tag $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
	else
		LOG "Stopping container: $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
		sudo docker stop $container_id

		LOG "Dropping container: $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
		sudo docker rm $container_id		
	fi

	local image_id=$(sudo docker images -f "label=application=$TOOLKIT_APPLICATION" -f "label=type=$TOOLKIT_TYPE" -f "dangling=true" -q --no-trunc)

	if [[ -z "$image_id" ]]; then
		LOG "There is not image with tag $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
	else
		LOG "Deleting image for $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
		sudo docker rmi $(echo $image_id)
	fi

	LOG "Building image: $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
	$(echo sudo docker build  --rm -t "$TOOLKIT_APPLICATION/$TOOLKIT_TYPE:$TOOLKIT_VERSION" "$TOOLKIT_ARGS" .)

	FOOTER
}

function .docker-run (){
	HEADER
	
	. ./toolkit.conf 

	local container_id=$(sudo docker ps -a -q -f "label=application=$TOOLKIT_APPLICATION" -f "label=type=$TOOLKIT_TYPE")

	if [[ -z "$container_id" ]]; then
		LOG "There is not container with tag $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
	else
		LOG "Stopping container: $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
		sudo docker stop $container_id

		LOG "Dropping container: $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
		sudo docker rm $container_id		
	fi

	LOG "Runngin container: $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
	$(echo sudo docker run "$TOOLKIT_PORTS" "$TOOLKIT_APPLICATION/$TOOLKIT_TYPE:$TOOLKIT_VERSION" )

	FOOTER
}

function .docker-start (){
	HEADER
	
	. ./toolkit.conf 

	local container_id=$(sudo docker ps -a -q -f "label=application=$TOOLKIT_APPLICATION" -f "label=type=$TOOLKIT_TYPE")

	LOG "Runngin container: $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
	$(echo sudo docker start "$container_id" )

	FOOTER
}

function .docker-stop (){
	HEADER
	
	. ./toolkit.conf 

	local container_id=$(sudo docker ps -a -q -f "label=application=$TOOLKIT_APPLICATION" -f "label=type=$TOOLKIT_TYPE")

	LOG "Stopping container: $TOOLKIT_APPLICATION:$TOOLKIT_TYPE"
	$(echo sudo docker stop "$container_id" )

	FOOTER
}