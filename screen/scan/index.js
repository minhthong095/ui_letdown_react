import styled from 'styled-components'
import { View, Text, TouchableOpacity, Modal } from 'react-native'
import React, { useState, useEffect, useRef } from 'react';
import { PermissionsAndroid, NativeModules, AppState } from 'react-native';
import { RNCamera } from 'react-native-camera'
import { Navigation, Stack } from '../../navigation/navigation';
const { OpenSetting } = NativeModules

const RNXCamera = props =>
    <RNCamera
        onCameraReady={_ => { console.log('B: ' + Math.floor(Date.now())) }}
        orientation={'portrait'}
        style={props.style}
        captureAudio={false}
        autoFocus={RNCamera.Constants.AutoFocus.off}
    />

export const Scan = () => {

    const [isOpenCamera, setOpenCamera] = useState(false);
    const [isAskPermission, setAskPermission] = useState(false);
    // const [isEnableScanBtn, setEnableScanBtn] = useState(false)
    const isAlreadyInit = useRef(null)

    useEffect(_ => {
        requestCameraCheckFirstime();
        AppState.addEventListener('change', (state) => {
            if (state == 'active' && isAlreadyInit.current != null)
                checkCameraPermission()

            isAlreadyInit.current = true
        })
    }, []);

    function checkCameraPermission() {
        PermissionsAndroid
            .check(PermissionsAndroid.PERMISSIONS.CAMERA)
            .then(checked => {
                if (!checked) setAskPermission(true)
                else setAskPermission(false)
            })
    }

    function requestCameraCheckFirstime() {
        PermissionsAndroid
            .request(PermissionsAndroid.PERMISSIONS.CAMERA)
            .then(result => {
                if (result != PermissionsAndroid.RESULTS.GRANTED)
                    setAskPermission(true)
            })
    }

    function openCameraWithPermissionGranted() {
        setAskPermission(false)
        console.log('A: ' + Math.floor(Date.now()))
        Navigation.navigate(Stack.ScanCamera)
    }

    function requestCameraToOpen() {
        PermissionsAndroid
            .request(PermissionsAndroid.PERMISSIONS.CAMERA)
            .then(result => {
                if (result == PermissionsAndroid.RESULTS.GRANTED)
                    openCameraWithPermissionGranted()
                else
                    setAskPermission(true)
            })
    }

    function requestCameraToCheck() {
        PermissionsAndroid
            .request(PermissionsAndroid.PERMISSIONS.CAMERA)
            .then(result => {
                if (result == PermissionsAndroid.RESULTS.DENIED)
                    setAskPermission(true)
                else if (result == PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN)
                    OpenSetting.openAppSetting()
                else setAskPermission(false)
            })
    }

    function openCamera() {
        PermissionsAndroid
            .check(PermissionsAndroid.PERMISSIONS.CAMERA)
            .then(check => {
                if (!check) requestCameraToOpen()
                else openCameraWithPermissionGranted()
            });
    }

    console.log('Render Bae');

    return (
        <Container>
            <ContainerVisualCamera>
                {isAskPermission &&
                    <PermissionView>
                        <AskPermissionText>YOU NEED TO APPROVE CAMERA PERMISSION.</AskPermissionText>
                        <TurnOnWrap>
                            <TouchableOpacity onPress={requestCameraToCheck}>
                                <TurnOnText>TURN ON</TurnOnText>
                            </TouchableOpacity>
                        </TurnOnWrap>
                    </PermissionView>
                }
            </ContainerVisualCamera>
            <TouchableOpacity onPress={openCamera}>
                <ScanButton>
                    <TextButton>SCAN</TextButton>
                </ScanButton>
            </TouchableOpacity>
        </Container>
    )
}

const ContainerVisualCamera = styled(View)`
            flex: 1;
            justify-content: center;
            align-items: center;
        `

const PermissionView = styled(View)`
    width: 200;
`

const TurnOnWrap = styled(View)`
    flex-direction: row;
`

const TurnOnText = styled(Text)`
            margin-top: 20;
            color: blue;
            font-size: 22;
        `
const AskPermissionText = styled(Text)`
            font-size: 22;
            color: black;
        `

const VisualCamera = styled(View)`
            height: 200;
            width: 200;
            background-color: green;
            justify-content: center;
            align-items: center;
        `

const StyledRNXCamera = styled(RNXCamera)`
            flex: 1;
        `

const Container = styled(View)`
            flex: 1;
            background-color: white;
            justify-content: flex-end;
        `

const ScanButton = styled(View)`
            width: 100%;
            height: 50;
            justify-content: center;
            align-items: center;
            background-color: black;
        `

const TextButton = styled(Text)`
           color: white;
           font-size: 22;
`