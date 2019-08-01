import styled from 'styled-components'
import React from 'react'
import { View, Text } from 'react-native'
import { RNCamera } from 'react-native-camera'

export const ScanCamera = _ => {
    console.log('Render bae!!!!');
    return (
        <RNCamera
            onCameraReady={_ => { console.log('B: ' + Math.floor(Date.now())) }}
            orientation={'portrait'}
            style={{ flex: 1 }}
            captureAudio={false}
            autoFocus={RNCamera.Constants.AutoFocus.off}
        />
    )
}

const Container = styled(View)`
    flex: 1;
    justify-content: center;
    align-items: center
`

const JustLabel = styled(Text)`
    color: black;
    font-size: 30;
`