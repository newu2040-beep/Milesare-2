package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.AnonymousPersona
import com.example.repository.AuthRepository
import com.example.repository.ConfessionRepository
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PostDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SavedPostsScreen
import com.example.ui.theme.NeonFuchsia
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.CreatePostViewModel
import com.example.ui.viewmodel.FeedViewModel
import com.example.ui.viewmodel.PostDetailViewModel
import com.example.ui.viewmodel.ProfileViewModel

object Routes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val CREATE_POST = "create_post"
    const val SAVED = "saved"
    const val PROFILE = "profile"
    const val POST_DETAIL = "post_detail/{postId}"
    const val EXPORT_STUDIO = "export_studio/{postId}"

    fun postDetail(postId: String) = "post_detail/$postId"
    fun exportStudio(postId: String) = "export_studio/$postId"
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    currentUserId: String,
    onSignOutComplete: () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val confessionRepository = remember { ConfessionRepository(context) }
    val authRepository = remember { AuthRepository(context) }

    val feedViewModel = remember { FeedViewModel(confessionRepository) }
    val profileViewModel = remember { ProfileViewModel(authRepository, confessionRepository) }
    val userProfile by authViewModel.userProfile.collectAsState()

    val bottomBarRoutes = setOf(Routes.HOME, Routes.EXPLORE, Routes.SAVED, Routes.PROFILE)
    val shouldShowBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = shouldShowBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // Home Tab
                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(Icons.Default.DynamicFeed, contentDescription = "Feed")
                        },
                        label = { Text("Feed") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    // Explore Tab
                    NavigationBarItem(
                        selected = currentRoute == Routes.EXPLORE,
                        onClick = {
                            navController.navigate(Routes.EXPLORE) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                if (currentRoute == Routes.EXPLORE) Icons.Default.Explore else Icons.Outlined.Explore,
                                contentDescription = "Explore"
                            )
                        },
                        label = { Text("Explore") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_explore")
                    )

                    // Center (+) Floating Action Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .offset(y = (-10).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingActionButton(
                            onClick = { navController.navigate(Routes.CREATE_POST) },
                            containerColor = Color.Transparent,
                            elevation = FloatingActionButtonDefaults.elevation(6.dp),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(VioletPrimary, NeonFuchsia)))
                                .testTag("center_create_fab")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Whisper Confession",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Saved Tab
                    NavigationBarItem(
                        selected = currentRoute == Routes.SAVED,
                        onClick = {
                            navController.navigate(Routes.SAVED) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                if (currentRoute == Routes.SAVED) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Saved"
                            )
                        },
                        label = { Text("Saved") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_saved")
                    )

                    // Profile Tab
                    NavigationBarItem(
                        selected = currentRoute == Routes.PROFILE,
                        onClick = {
                            navController.navigate(Routes.PROFILE) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                if (currentRoute == Routes.PROFILE) Icons.Default.Person else Icons.Default.PersonOutline,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    feedViewModel = feedViewModel,
                    userProfile = userProfile,
                    currentUserId = currentUserId,
                    onNavigateToPostDetail = { postId ->
                        navController.navigate(Routes.postDetail(postId))
                    },
                    onNavigateToCreatePost = {
                        navController.navigate(Routes.CREATE_POST)
                    },
                    onNavigateToExplore = {
                        navController.navigate(Routes.EXPLORE)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Routes.PROFILE)
                    },
                    onNavigateToExport = { postId ->
                        navController.navigate(Routes.exportStudio(postId))
                    }
                )
            }

            composable(Routes.EXPLORE) {
                ExploreScreen(
                    feedViewModel = feedViewModel,
                    currentUserId = currentUserId,
                    onNavigateToPostDetail = { postId ->
                        navController.navigate(Routes.postDetail(postId))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToExport = { postId ->
                        navController.navigate(Routes.exportStudio(postId))
                    }
                )
            }

            composable(Routes.CREATE_POST) {
                val initialPersona = userProfile?.let {
                    AnonymousPersona(it.anonymousName, it.anonymousAvatar)
                }
                val createPostViewModel = remember {
                    CreatePostViewModel(confessionRepository, initialPersona)
                }

                CreatePostScreen(
                    createPostViewModel = createPostViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPostCreated = { postId ->
                        navController.popBackStack()
                        navController.navigate(Routes.postDetail(postId))
                    }
                )
            }

            composable(Routes.SAVED) {
                SavedPostsScreen(
                    profileViewModel = profileViewModel,
                    currentUserId = currentUserId,
                    onNavigateToPostDetail = { postId ->
                        navController.navigate(Routes.postDetail(postId))
                    }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    profileViewModel = profileViewModel,
                    currentUserId = currentUserId,
                    onNavigateToPostDetail = { postId ->
                        navController.navigate(Routes.postDetail(postId))
                    },
                    onNavigateToExport = { postId ->
                        navController.navigate(Routes.exportStudio(postId))
                    },
                    onSignOutComplete = onSignOutComplete
                )
            }

            composable(
                route = Routes.POST_DETAIL,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                val postDetailViewModel = remember(postId) {
                    PostDetailViewModel(postId, confessionRepository)
                }

                PostDetailScreen(
                    postDetailViewModel = postDetailViewModel,
                    userProfile = userProfile,
                    currentUserId = currentUserId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToExport = { expPostId ->
                        navController.navigate(Routes.exportStudio(expPostId))
                    }
                )
            }

            composable(
                route = Routes.EXPORT_STUDIO,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                val postState by confessionRepository.observePost(postId).collectAsState(initial = null)

                postState?.let { post ->
                    com.example.ui.screens.ExportStudioScreen(
                        post = post,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
