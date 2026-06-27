package com.example.upionemoretime.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.upionemoretime.screens.BalanceScreen
import com.example.upionemoretime.screens.HomeScreen
import com.example.upionemoretime.screens.PaymentScreen
import com.example.upionemoretime.screens.RechargeScreen
import com.example.upionemoretime.screens.SettingsScreen
import com.example.upionemoretime.screens.HistoryScreen
import com.example.upionemoretime.screens.StatsScreen
import com.example.upionemoretime.screens.ScanQRScreen
import com.example.upionemoretime.screens.LoginScreen
import com.example.upionemoretime.screens.SignUpScreen
import com.example.upionemoretime.screens.OtpVerificationScreen
import com.example.upionemoretime.screens.VoiceEnrollmentScreen
import com.example.upionemoretime.voice.VoiceManager

@Composable
fun AppNavigation(
    voiceManager: VoiceManager,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {

    val navController = rememberNavController()

    LaunchedEffect(navController) {
        voiceManager.updateNavController(navController)
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SIGNUP
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(navController, voiceManager)
        }

        composable(Routes.SIGNUP) {
            SignUpScreen(navController, voiceManager)
        }

        composable(Routes.VOICE_ENROLLMENT) {
            VoiceEnrollmentScreen(navController, voiceManager)
        }

        composable(
            route = Routes.OTP_VERIFICATION,
            arguments = listOf(
                navArgument("mobileNumber") { type = NavType.StringType },
                navArgument("isLogin") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val mobileNumber = backStackEntry.arguments?.getString("mobileNumber") ?: ""
            val isLogin = backStackEntry.arguments?.getBoolean("isLogin") ?: true
            OtpVerificationScreen(navController, voiceManager, mobileNumber, isLogin)
        }

        composable(Routes.HISTORY) {
            HistoryScreen(navController, voiceManager)
        }

        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
                voiceManager = voiceManager,
                isDarkMode = isDarkMode,
                onDarkModeChange = onDarkModeChange
            )
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
                navController = navController,
                voiceManager = voiceManager
            )
        }

        composable(Routes.BALANCE) {
            BalanceScreen(navController, voiceManager)
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
            RechargeScreen(
                navController = navController,
                voiceManager = voiceManager,
                initialMobileNumber = mobileNumber,
                initialAmount = amount
            )
        }
        composable(
            route = Routes.RECHARGE
        ) {
            RechargeScreen(
                navController = navController,
                voiceManager = voiceManager
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(navController, voiceManager)
        }
        composable(Routes.STATS) {
            StatsScreen(navController, voiceManager)
        }
        composable(Routes.SCAN_QR) {
            ScanQRScreen(navController, voiceManager)
        }
    }
}