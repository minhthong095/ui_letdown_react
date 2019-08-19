import styled from 'styled-components'
import { View, Text, TouchableOpacity, Modal, StyleSheet } from 'react-native'
import React, { useState, useEffect, useRef } from 'react';
import { PermissionsAndroid, NativeModules, AppState, requireNativeComponent } from 'react-native';
import { Navigation, Stack } from '../../navigation/navigation';
import { BarCodeCamera, BarCodeCameraType } from '../../component/bar_code_camera';
// import CheckBox from '../../component/checkbox/checkbox';
const AndroidCheckBox = requireNativeComponent('AndroidCheckBox')

const { OpenSetting } = NativeModules

export const Scan = () => {

    const [isAskPermission, setAskPermission] = useState(false);
    const [isEnableScanBtn, setEnableScanBtn] = useState(false)
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
                if (!checked) {
                    setAskPermission(true)
                    isEnableScanBtn.current = false
                }
                else {
                    setAskPermission(false)
                    setEnableScanBtn(true)
                }
            })
    }

    function requestCameraCheckFirstime() {
        PermissionsAndroid
            .request(PermissionsAndroid.PERMISSIONS.CAMERA)
            .then(result => {
                if (result != PermissionsAndroid.RESULTS.GRANTED)
                    setAskPermission(true)

                if (result == PermissionsAndroid.RESULTS.GRANTED) {
                    setEnableScanBtn(true)
                }
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
                else {
                    setAskPermission(false)
                    setEnableScanBtn(true)
                }
            })
    }

    function _openCamera() {
        if (isEnableScanBtn)
            Navigation.navigate(Stack.ScanCamera)
    }

    function getActiveScanBtn() {
        return isEnableScanBtn == true ? 0.2 : 1
    }

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
            <TouchableOpacity style={{ width: 100, height: 100, backgroundColor: 'green' }} />
            <TouchableOpacity activeOpacity={getActiveScanBtn()} onPress={_openCamera}>
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