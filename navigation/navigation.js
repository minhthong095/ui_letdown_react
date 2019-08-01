import { NavigationActions } from 'react-navigation'

var index = 0

// init
function i() {
    return (index++).toString();
}

const Stack = {
    Scan: i(),
    ScanCamera: i()
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