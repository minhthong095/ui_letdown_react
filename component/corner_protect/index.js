import React from 'react'
import { View } from 'react-native'
import styled from 'styled-components'
import PropTypes from 'prop-types'

export const CornerProtect = props => {
    return (
        <Container {...props}>
            <UpDraw {...props} />
            <DownDraw {...props} />
        </Container>
    )
}

const Container = styled(View)`
    width: ${props => props.width}
    height: ${props => props.height}
`

const UpDraw = styled(View)`
    position: absolute;
    width: ${props => props.width}
    height: ${props => props.cornerWidth};
    background-color: ${props => props.colorStyle}
`

const DownDraw = styled(View)`
    position: absolute;
    width: ${props => props.cornerWidth};
    height: ${props => props.height}
    background-color: ${props => props.colorStyle}
`

CornerProtect.defaultProps = {
    type: 'topLeft',
    width: 11,
    height: 11,
    colorStyle: 'black',
    cornerWidth: 2
}

CornerProtect.propTypes = {
    type: PropTypes.string,
    width: PropTypes.number,
    height: PropTypes.number,
    colorStyle: PropTypes.string,
    cornerWidth: PropTypes.number
}