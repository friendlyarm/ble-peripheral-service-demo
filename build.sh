#!/bin/bash

CURRENTPATH=$PWD
if [ ! -d $CURRENTPATH/golang/go-arm443 ]; then
	cd $CURRENTPATH/golang
	./golang-build-arm443.sh
	cd -
fi

export GOROOT=$CURRENTPATH/golang/go-arm443
export GOBIN=$CURRENTPATH/golang/go-arm443/bin/go
export CC=/opt/FriendlyARM/toolschain/4.5.1/bin/arm-linux-gcc

export GOPATH=$CURRENTPATH
GOARM=5 CGO_ENABLED=1 GOARCH=arm go build src/github.com/paypal/gatt/examples/nanopi_ble_server.go


