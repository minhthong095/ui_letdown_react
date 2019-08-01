import { NavigationActions } from 'react-navigation'

const Stack = {
    Scan: 'Scan',
    ScanCamera: 'ScanCamera',
}

var navigation;

function setTopLevelNavigator(node) {
    navigation = node;
}

function navigate(routeName, params = {}) {
    navigation.dispatch(NavigationActions.navigate({ routeName: routeName, params: params }));
}

function goBack() {
    navigation.dispatch(NavigationActions.back());
}

const Navigation = {
    setTopLevelNavigator, navigate, goBack
}

export { Navigation, Stack }