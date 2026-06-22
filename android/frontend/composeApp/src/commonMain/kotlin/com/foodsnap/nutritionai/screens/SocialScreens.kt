package com.foodsnap.nutritionai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foodsnap.nutritionai.theme.*

data class LeaderboardUser(val rank: Int, val name: String, val streak: Int, val xp: Int)
data class CommunityPost(val name: String, val avatar: String, val time: String, val message: String, val likes: Int)

@Composable
fun SocialDashboardScreen(
    onBack: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToChallenges: () -> Unit,
    onNavigateToAchievements: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    val posts = listOf(
        CommunityPost("Jane Cooper", "🥗", "10m ago", "Just scanned my breakfast bowl. Met all my macros! Highly recommend adding chia seeds.", 24),
        CommunityPost("Wade Warren", "🏃", "1h ago", "Hit a new 10,000 steps streak today! Feeling amazing.", 42),
        CommunityPost("Kristin Watson", "🥑", "3h ago", "Avocado toast hack: add lemon juice and crushed red pepper for extra metabolic boost.", 15)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface)
            ) {
                Text("← Back", color = LightText)
            }
            Text("Social & Rewards", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightText)
            Spacer(modifier = Modifier.width(60.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Links to Sub-sections
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onNavigateToLeaderboard,
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                modifier = Modifier.weight(1f)
            ) {
                Text("🏆 Board", fontSize = 12.sp, color = LightText)
            }
            Button(
                onClick = onNavigateToChallenges,
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                modifier = Modifier.weight(1f)
            ) {
                Text("🎯 Tasks", fontSize = 12.sp, color = LightText)
            }
            Button(
                onClick = onNavigateToAchievements,
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                modifier = Modifier.weight(1f)
            ) {
                Text("🏅 Badges", fontSize = 12.sp, color = LightText)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Community Feed", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            posts.forEach { post ->
                CommunityPostCard(post)
            }
        }
    }
}

@Composable
fun CommunityPostCard(post: CommunityPost) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GlassSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(post.avatar, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(post.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightText)
                Text(post.time, fontSize = 11.sp, color = GrayText)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(post.message, fontSize = 13.sp, color = LightText, lineHeight = 18.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("❤️", fontSize = 14.sp, modifier = Modifier.padding(end = 4.dp))
            Text("${post.likes} Likes", fontSize = 11.sp, color = GrayText)
        }
    }
}

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val users = listOf(
        LeaderboardUser(1, "Wade Warren", 14, 2450),
        LeaderboardUser(2, "Jane Cooper", 7, 1820),
        LeaderboardUser(3, "Alex Sterling (You)", 7, 1250),
        LeaderboardUser(4, "Kristin Watson", 5, 980),
        LeaderboardUser(5, "Guy Hawkins", 2, 750)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = GlassSurface)) {
                Text("← Back", color = LightText)
            }
            Text("Leaderboard Rankings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightText)
            Spacer(modifier = Modifier.width(60.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(users) { user ->
                val isMe = user.name.contains("You")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isMe) PrimaryPurple.copy(alpha = 0.15f) else GlassSurface)
                        .border(1.dp, if (isMe) PrimaryPurple else GlassBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (user.rank) {
                            1 -> "🥇"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> "${user.rank}"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightText,
                        modifier = Modifier.width(36.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightText)
                        Text("🔥 ${user.streak} Day Streak", fontSize = 11.sp, color = GrayText)
                    }
                    Text("${user.xp} XP", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                }
            }
        }
    }
}

@Composable
fun ChallengesScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = GlassSurface)) {
                Text("← Back", color = LightText)
            }
            Text("Active Challenges", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightText)
            Spacer(modifier = Modifier.width(60.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ChallengeCard("Hydration Guru", "Drink at least 2.8L water daily for 7 days in a row.", "6/7 days", 0.85f, 250)
            ChallengeCard("Perfect Macros", "Reach your target macro parameters within 5% error.", "2/3 times", 0.66f, 150)
            ChallengeCard("Workout Warrior", "Complete 3 distinct calorie burn exercises this week.", "1/3 exercises", 0.33f, 200)
        }
    }
}

@Composable
fun ChallengeCard(title: String, desc: String, progressText: String, ratio: Float, reward: Int) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LightText)
        Text(desc, fontSize = 11.sp, color = GrayText, modifier = Modifier.padding(top = 4.dp))
        
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Progress: $progressText", fontSize = 11.sp, color = GrayText)
            Text("+$reward XP", fontSize = 11.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .fillMaxHeight()
                    .background(PrimaryPurple, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun AchievementsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = GlassSurface)) {
                Text("← Back", color = LightText)
            }
            Text("Achievement Center", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightText)
            Spacer(modifier = Modifier.width(60.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AchievementBadge("First Scan", "Successfully scanned your first meal using the camera.", "📸", 50)
            AchievementBadge("Water Master", "Hit your hydration target for 3 consecutive days.", "💧", 100)
            AchievementBadge("Streak Starter", "Achieved a 7-day log streak.", "🔥", 200)
            AchievementBadge("Calorie Controller", "Kept net calories below daily goal limit.", "🎯", 150)
        }
    }
}
