import React from 'react'
import { RNCamera } from 'react-native-camera'
import styled from 'styled-components'
import { View, Image, TouchableOpacity } from 'react-native'
import { Url } from '../../global/url';

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
            <TouchableOpacity>
                <Img source={Url.IMG_ABORT} />
            </TouchableOpacity>
        </Container>
    )
}

const Container = styled(View)`
            flex: 1;
            background-color: black;
`

const Img = styled(Image)`
    width: 40;
    height: 40;
    margin-left: 20;
    margin-top: 20;
`