import React, { useState } from 'react'
import styled from 'styled-components'
import { View, Image, TouchableOpacity, StyleSheet, NativeModules, Text, requireNativeComponent, Alert } from 'react-native'
import { ImgUrl } from '../../global/img_url';
import { FlashOnOff } from '../../component/flash_on_off';
const BarCodeCameraView = requireNativeComponent('BarCodeCameraView')
const PlaygroundView = requireNativeComponent('PlaygroundView')

export const ScanCamera = _ => {

    const [onOffCam, setOnOffCam] = useState(true)

    return (
        <Container>
            {/* <RNCamera
                onCameraReady={_ => { console.log('B: ' + Math.floor(Date.now())) }}
                orientation={'portrait'}
                style={{ flex: 1 }}
                captureAudio={false}
                autoFocus={RNCamera.Constants.AutoFocus.off}
            />
            <View
                pointerEvents={'none'}
                style={{
                    borderWidth: 1,
                    borderColor: 'yellow',
                    position: 'absolute',
                    left: 100,
                    top: 100,
                    width: 20,
                    height: 20
                }}
            /> */}
            {/* <TouchableOpacity>
                <Img source={Url.ABORT} />
            </TouchableOpacity> */}
            {/* <BlurMask />
            <TouchableOpacity style={styles.touchableAbort}
                onPress={_ => { console.log('haha') }}>
                <AbortX source={ImgUrl.ABORT_X} />
            </TouchableOpacity>
            <SFlashOnOff /> */}
            {/* {onOffCam && <BarCodeCameraView style={StyleSheet.absoluteFill} />} */}
            {onOffCam && <PlaygroundView onClick={ass => {
                console.log('ASS: ' + ass)
            }} style={StyleSheet.absoluteFill} />}
            <TouchableOpacity onPress={_ => { setOnOffCam(!onOffCam) }} style={{ position: 'absolute', left: 0, bottom: 0, right: 0 }}>
                <View style={{ flex: 1, backgroundColor: 'black' }}>
                    <Text style={{ paddingTop: 30, color: 'white' }}>HIT</Text>
                </View>
            </TouchableOpacity>
        </Container>
    )
}

const styles = StyleSheet.create({
    touchableAbort: {
        top: 20,
        left: 20,
        position: 'absolute'
    }
})

const BlurMask = styled(View)`
    flex: 1;
    background-color: black;
    opacity: 0.5;
`

const SFlashOnOff = styled(FlashOnOff)`
    position: absolute;
    top: 20;
    right: 20;
`

const AbortX = styled(Image)`
    width: 42;
    height: 42
`
const Container = styled(View)`
    flex: 1;
    flex-direction: row
`