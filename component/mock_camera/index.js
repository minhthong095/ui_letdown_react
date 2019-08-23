import React, { useState } from 'react'
import styled from 'styled-components'
import { View, Image, TouchableOpacity, StyleSheet, NativeModules, Text, requireNativeComponent, Alert } from 'react-native'
import { ImgUrl } from '../../global/img_url';
import { FlashOnOff } from '../flash_on_off';
import { BarCodeCamera, BarCodeCameraType } from '../bar_code_camera';

export const MockCamera = _ => {

    return (
        <Container>
            <SampleText>SAMPLE CAMERA</SampleText>
        </Container>
    )
}

const SampleText = styled(Text)`
    color: yellow;
    font-size: 20px;
`

const Container = styled(View)`
    flex: 1;
    flex-direction: row;
    border-width: 5;
    border-color: yellow;
    justify-content: center;
    align-items: center;
    background-color: green;
`