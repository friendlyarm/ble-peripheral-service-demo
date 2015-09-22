#!/bin/bash

rm -rf go-arm47
tar xvzf go1.4.2.linux-386.tar.gz 
mv go go-arm47
export GOROOT=$PWD/go-arm47
export PATH=$PATH:$GOROOT/bin
CC=/opt/FriendlyARM/toolschain/arm-linux-gnueabihf-4.7/bin/arm-linux-gnueabihf-gcc
cd go-arm47/src/
CGO_ENABLED=0 GOARCH=arm GOOS=linux ./make.bash
