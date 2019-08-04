import React, { useState } from 'react'
import { RNCamera } from 'react-native-camera'
import styled from 'styled-components'
import { View, Image, TouchableOpacity, StyleSheet } from 'react-native'
import { ImgUrl } from '../../global/img_url';
import { FlashOnOff } from '../../component/flash_on_off';
export const ScanCamera = _ => {
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
            <BlurMask />
            <TouchableOpacity style={styles.touchableAbort}
                onPress={_ => { console.log('haha') }}>
                <AbortX source={ImgUrl.ABORT_X} />
            </TouchableOpacity>
            <SFlashOnOff />
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
    background-color: green;
    flex: 1;
    flex-direction: row
`