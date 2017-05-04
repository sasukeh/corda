'use strict';

define(['angular', 'utils/semantic', 'services/NodeApi', 'services/HttpErrorHandler'], (angular, semantic) => {
    angular.module('irsViewer').controller('HomeController', function HomeController($http, $scope, nodeService, httpErrorHandler) {
        semantic.addLoadingModal($scope, nodeService.isLoading);

        let handleHttpFail = httpErrorHandler.createErrorHandler($scope);

        $scope.infoMsg = "";
        $scope.errorText = "";
        $scope.date = { "year": "...", "month": "...", "day": "..." };
        $scope.updateDate = (type) => {
            nodeService.updateDate(type).then((newDate) => {
                $scope.date = newDate
            }, handleHttpFail);
        };

        nodeService.getDate().then((date) => $scope.date = date, handleHttpFail);
        nodeService.getDeals().then((deals) => $scope.deals = deals, handleHttpFail);
    });
});