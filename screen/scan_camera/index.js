import React from 'react'
import { RNCamera } from 'react-native-camera'

export const ScanCamera = _ => {
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