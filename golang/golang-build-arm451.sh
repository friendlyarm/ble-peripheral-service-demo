#!/bin/bash

rm -rf go-arm451
tar xvzf go1.4.2.linux-386.tar.gz 
mv go go-arm451
export GOROOT=$PWD/go-arm451
export PATH=$PATH:$GOROOT/bin
CC=/opt/FriendlyARM/toolschain/4.5.1/bin/arm-linux-gcc
cd go-arm451/src/
CGO_ENABLED=0 GOARCH=arm GOOS=linux ./make.bash
