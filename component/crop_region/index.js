import React, { useRef, useLayoutEffect, useEffect, useState, Fragment } from 'react'
import styled from 'styled-components'
import { View } from 'react-native'
import PropTypes from 'prop-types';
import { CornerProtect } from '../corner_protect'

export const CropRegion = props => {

    return (
        <Container>
            <Top {...props} />
            <CropLeft {...props} />
            <CropBorder {...props}>
                <CornerProtect />
                <CropProtectTopRight type={'topRight'} />
                <CropProtectBottomLeft type={'bottomLeft'} />
                <CropProtectBottomRight type={'bottomRight'} />
            </CropBorder>
            <CropRight {...props} />
            <Bottom {...props} />
        </Container>
    )
}

const Container = styled(View)`
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
`

const CropProtectTopRight = styled(CornerProtect)`
  position: absolute;
  top: 0;
  right: 0
`

const CropProtectBottomLeft = styled(CornerProtect)`
  position: absolute;
  bottom: 0;
  left: 0
`

const CropProtectBottomRight = styled(CornerProtect)`
  position: absolute;
  bottom: 0;
  right: 0
`

const CropBorder = styled(View)`
    position: absolute;
    top: ${props => props.yCrop}
    left: ${props => props.xCrop}
    width: ${props => props.widthCrop}
    height: ${props => props.heightCrop}
    border-width: 0.2;
    border-color: white;
`

const OpacityBlack = styled(View)`
    background-color: black;
    opacity: 0.17;
`

const Top = styled(OpacityBlack)`
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: ${props => props.yCrop}
`

const CropLeft = styled(OpacityBlack)`
    position: absolute;
    top: ${props => props.yCrop}
    left: 0;
    width: ${props => props.xCrop}
    height: ${props => props.heightCrop}
`

const CropRight = styled(OpacityBlack)`
    position: absolute;
    top: ${props => props.yCrop}
    right: 0;
    width: ${props => props.xCrop}
    height: ${props => props.heightCrop}
`

const Bottom = styled(OpacityBlack)`
    position: absolute;
    height: ${props => props.heightContainer - (props.yCrop + props.heightCrop)}
    bottom: 0;
    left: 0;
    right: 0;
`

CropRegion.propTypes = {
    widthCrop: PropTypes.number,
    heightCrop: PropTypes.number,
    widthContainer: PropTypes.number,
    heightContainer: PropTypes.number,
    yCrop: PropTypes.number,
    xCrop: PropTypes.number
}