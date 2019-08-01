import styled from 'styled-components'
import { View, Text, TouchableOpacity, Alert } from 'react-native'
import React, { useState, useEffect, useRef } from 'react';
import { PermissionsAndroid, NativeModules, AppState } from 'react-native';
import { RNCamera } from 'react-native-camera'
const { OpenSetting } = NativeModules

function onPress() {
    console.log('Ahihi');
}

const RNXCamera = props =>
    <RNCamera
        orientation={'portrait'}
        style={props.style}
        captureAudio={false}
        autoFocus={RNCamera.Constants.AutoFocus.off}
    />

export const Scan = () => {

    const [isOpenCamera, setOpenCamera] = useState(false);
    const [isAskPermission, setAskPermission] = useState(false);
    const isAlreadyInit = useRef(null)

    useEffect(_ => {
        console.log('Effect Bae!!');
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
        setOpenCamera(true)
        setAskPermission(false)
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

    console.log('Render Bae!!');

    return (
        <Container>
            {/* <RNXStyledCamera /> */}
            <ContainerVisualCamera>
                {isOpenCamera && <VisualCamera />}
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
            background-color: transparent;
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

const AskPermission = styled(View)`
            width: 100;
            height: 200;
            background-color: black;
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

const RNXStyledCamera = styled(RNXCamera)`
            flex: 1;
            justify-content: flex-end;
            align-items: center;
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