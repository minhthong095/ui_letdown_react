import { StackActions } from 'react-navigation'

const Stack = {
    Scan: 'Scan',
    ScanCamera: 'ScanCamera',
}

var navigation;

function setTopLevelNavigator(node) {
    navigation = node;
}

function stackNavigate(routeName, params = {}) {
    navigation.dispatch(StackActions.push({ routeName: routeName, params: params }));
}

function stackPop() {
    navigation.dispatch(StackActions.pop({ n: 1 }));
}

function getState() {
    return navigation.statstate
}

const Navigation = {
    setTopLevelNavigator, stackNavigate, stackPop, getState
}

export { Navigation, Stack }