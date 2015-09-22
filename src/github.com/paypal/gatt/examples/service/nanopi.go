package service

import (
	"fmt"
	"github.com/paypal/gatt"
)

func NewNanoPiBLEDemoService() *gatt.Service {
	data := make([]byte, 256)
	s := gatt.NewService(gatt.MustParseUUID("09fc95c0-c111-11e3-9904-0002a5d5c51b"))
	s.AddCharacteristic(gatt.MustParseUUID("11fac9e0-c111-11e3-9246-0002a5d5c51b")).HandleReadFunc(
		func(rsp gatt.ResponseWriter, req *gatt.ReadRequest) {
			fmt.Printf("readdata: ")
			for i := 0; i < len(data); i++ {
				fmt.Printf("%x ", data[i])
			}
			fmt.Printf("\n")
			fmt.Fprintf(rsp, fmt.Sprintf("%X", data))
		})

	c := s.AddCharacteristic(gatt.MustParseUUID("16fe0d80-c111-11e3-b8c8-0002a5d5c51b"))
	c.HandleWriteFunc(
		func(r gatt.Request, newData []byte) (status byte) {
			for i := 0; i < len(data) && i < len(newData); i++ {
				data[i] = newData[i]
			}
			fmt.Printf("writedata: ")
			for i := 0; i < len(newData); i++ {
				fmt.Printf("%x ", newData[i])
			}
			fmt.Printf("\n")
			return gatt.StatusSuccess
		})

	return s
}
