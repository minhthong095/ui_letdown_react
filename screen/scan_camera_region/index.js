import React, { useState } from 'react'
import styled from 'styled-components'
import { View, Image, TouchableOpacity, StyleSheet, NativeModules, Text, requireNativeComponent, Alert } from 'react-native'
import { ImgUrl } from '../../global/img_url';
import { FlashOnOff } from '../../component/flash_on_off';
import { BarCodeCamera, BarCodeCameraType } from '../../component/bar_code_camera';
import { RNCamera } from 'react-native-camera'
import { CropRegion } from '../../component/crop_region';
import { MockCamera } from '../../component/mock_camera';

export const ScanCameraRegion = _ => {

    const [onOffCam, setOnOffCam] = useState(true)
    const [barcodeSupport, setBarcodeSupport] = useState([BarCodeCameraType.CODE_128])

    return (
        <Container>
            <MockCamera />
            <CropRegion

            />
            <TouchableOpacityAbort onPress={_ => { console.log('haha') }}>
                <AbortX source={ImgUrl.ABORT_X} />
            </TouchableOpacityAbort>
            <Flash />
        </Container>
    )
}

const TouchableOpacityAbort = styled(TouchableOpacity)`
    top: 20;
    left: 20;
    position: absolute;
`

const BlurMask = styled(View)`
    flex: 1;
    background-color: black;
    opacity: 0.5;
`

const Flash = styled(FlashOnOff)`
    position: absolute;
    top: 20;
    right: 20;
`

const AbortX = styled(Image)`
    width: 40;
    height: 40
`
const Container = styled(View)`
    flex: 1;
    flex-direction: row;
`