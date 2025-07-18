package com.example.toolsapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.toolsapp.ui.theme.ToolsAppTheme
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.toolsapp.model.Utils
import com.example.toolsapp.model.classes.AppSettingsManager
import com.example.toolsapp.model.classes.Destination
import com.example.toolsapp.model.classes.ToolsDestination
import com.example.toolsapp.ui.screens.EventTimersScreen
import com.example.toolsapp.ui.screens.ParticleScreen
import com.example.toolsapp.ui.screens.ProfileScreen
import com.example.toolsapp.ui.screens.SettingsScreen
import com.example.toolsapp.ui.screens.TodoScreen
import com.example.toolsapp.ui.screens.ToolsScreen
import com.example.toolsapp.ui.viewModels.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private var saveCallback: () -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToolsAppTheme{
                val userViewModel by viewModels<UserViewModel>()
                val authState by userViewModel.authState.collectAsState()
                var dataLoaded by remember { mutableStateOf(false) }
                var ignoreInternetCheck by remember { mutableStateOf(false) }
                val context = LocalContext.current

                saveCallback = {
                    userViewModel.saveUserDataToDatabase(userViewModel.getCurrentUserId() ?: "",userViewModel.userData.value)
                }

                if (!ignoreInternetCheck && !checkForInternetConnection(context)) {
                    InternetRequiredScreen(
                        onRetry = {
                            if (checkForInternetConnection(context)) {
                                ignoreInternetCheck = false
                                Utils.setConnectedToInternet(true)
                            }
                        },
                        onIgnore = {
                            ignoreInternetCheck = true
                            Utils.setConnectedToInternet(false)
                        }
                    )
                } else {
                    if(!ignoreInternetCheck)
                        Utils.setConnectedToInternet(true)

                    LaunchedEffect(authState) {
                        if (authState) {
                            loadDataFromDatabase(userViewModel.getCurrentUserId() ?: "", userViewModel) {
                                dataLoaded = true
                            }
                        }
                    }

                    if (authState) {
                        if (dataLoaded || ignoreInternetCheck) {
                            MainApp()
                        } else {
                            LoadingScreen()
                        }
                    } else {
                        LoginScreen(
                            onLoginSuccess = { userId, code, mail ->
                                AppSettingsManager.updateUserId(userId)
                                if(code == "CONNECTED"){
                                    loadDataFromDatabase(userId, userViewModel) {
                                        dataLoaded = true
                                    }
                                }else{
                                    userViewModel.emptyUserData()
                                    AppSettingsManager.createNewSettings()
                                    dataLoaded = true
                                }
                            },
                            onLoginFailure = {  }
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        saveSettingsToDatabase()
    }

    fun loadDataFromDatabase(userId: String, userViewModel: UserViewModel, onLoaded: () -> Unit){
        var id = userId
        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        if(id.isEmpty() && firebaseAuth.currentUser != null){
            id = firebaseAuth.currentUser!!.uid
        }

        userViewModel.loadUserDataFromDatabase(id){}

        AppSettingsManager.loadSettings(id){
            onLoaded()
        }
    }

    fun saveSettingsToDatabase() {
        AppSettingsManager.saveSettings()
        saveCallback()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()

    val navigationItems = listOf(
        NavigationItem(
            title = stringResource(R.string.profile_nav),
            icon = Icons.Default.AccountCircle,
            route = Destination.Profile.route
        ),
        NavigationItem(
            title = stringResource(R.string.home_nav),
            icon = Icons.Default.Home,
            route = Destination.ToolsList.route
        ),
        NavigationItem(
            title = stringResource(R.string.settings_nav),
            icon = Icons.Default.Settings,
            route = Destination.Settings.route
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var selectedNavigationIndex = navigationItems.indexOfFirst {
        it.route == (currentRoute ?: Destination.ToolsList.route)
    }

    val showBackButton = currentRoute !in listOf(
        Destination.Profile.route,
        Destination.ToolsList.route,
        Destination.Settings.route
    )

    val allDestinations = listOf(
        Destination.Profile,
        Destination.ToolsList,
        Destination.Settings,
        *ToolsDestination.all.toTypedArray()
    )

    val routeTitles = allDestinations.associate { it.route to it.title }
    val currentTitle = currentRoute?.let { routeTitles[it] } ?: "Mon Application"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                                selectedNavigationIndex =
                                    navigationItems.indexOfFirst { it.route == navController.currentBackStackEntry?.destination?.route }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                navigationItems.forEachIndexed{index, item ->
                    NavigationBarItem(
                        selected = selectedNavigationIndex == index,
                        onClick = {
                            selectedNavigationIndex = index
                            navController.navigate(item.route) {
//                                popUpTo(navController.graph.findStartDestination().id) {
//                                    saveState = true
//                                }
                                popUpTo(item.route) { inclusive = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(imageVector = item.icon, contentDescription = item.title)
                        },
                        label = {
                            Text(
                                item.title,
                                color = if(selectedNavigationIndex == index) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.ToolsList.route,
            modifier = Modifier.padding(innerPadding),

            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(Destination.Profile.route) {
                ProfileScreen(
                )
            }

            composable(Destination.ToolsList.route) {
                ToolsScreen(
                    tools = ToolsDestination.all,
                    onToolClick = { navController.navigate(it.route) }
                )
            }

            composable(Destination.Settings.route) {
                SettingsScreen()
            }

            composable(ToolsDestination.TodoList.route) {
                TodoScreen(
                    onBack = { navController.popBackStack()}
                )
            }

            composable(ToolsDestination.EventTimers.route) {
                EventTimersScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ToolsDestination.Particles.route) {
                ParticleScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showBackButton) {
        BackHandler {
            navController.popBackStack()
            selectedNavigationIndex =
                navigationItems.indexOfFirst { it.route == navController.currentBackStackEntry?.destination?.route }
        }
    }
}

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun LoadingScreen(){
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.loading_anim))
        Spacer(modifier = Modifier.height(128.dp))
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .requiredSize(256.dp)
        )

        Text(
            stringResource(R.string.loading_data),
            modifier = Modifier
        )
    }
}

@Composable
fun InternetRequiredScreen(
    onRetry: () -> Unit,
    onIgnore: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(stringResource(R.string.please_connect_to_internet))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onIgnore) {
                Text(stringResource(R.string.ignore))
            }
        }
    }
}

private fun checkForInternetConnection(context: Context): Boolean{
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

    return when{
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }
}

