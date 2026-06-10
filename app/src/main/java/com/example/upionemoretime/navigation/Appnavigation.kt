package com.example.upionemoretime.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.upionemoretime.screens.BalanceScreen
import com.example.upionemoretime.screens.HomeScreen
import com.example.upionemoretime.screens.PaymentScreen
import com.example.upionemoretime.screens.RechargeScreen
import com.example.upionemoretime.screens.SettingsScreen
import com.example.upionemoretime.screens.HistoryScreen
import com.example.upionemoretime.screens.StatsScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HISTORY) {
            HistoryScreen(navController)
        }

        composable(Routes.HOME) {
            HomeScreen(navController)
        }

        composable(
            route = Routes.PAYMENT
        ) { backStackEntry ->

            val amount =
                backStackEntry.arguments
                    ?.getString("amount")
                    ?.toIntOrNull()
                    ?: 0

            val receiver =
                backStackEntry.arguments
                    ?.getString("receiver")
                    ?: ""

            PaymentScreen(
                amount = amount,
                receiver = receiver,
                        navController = navController
            )
        }

        composable(Routes.BALANCE) {
            BalanceScreen(navController)
        }

        composable(route = Routes.RECHARGE_ROUTE) {
                backStackEntry ->
            val mobileNumber =
                backStackEntry.arguments
                    ?.getString("mobileNumber")
                    ?: ""

            val amount =
                backStackEntry.arguments
                    ?.getString("amount")
                    ?.toIntOrNull()
                    ?: 0
            RechargeScreen(navController= navController,
                initialMobileNumber = mobileNumber,
                initialAmount = amount)
        }
        composable(
            route = Routes.RECHARGE
        ) {
            RechargeScreen(
                navController = navController
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(navController)
        }
        composable(Routes.STATS) {
            StatsScreen(navController)
        }
    }
}