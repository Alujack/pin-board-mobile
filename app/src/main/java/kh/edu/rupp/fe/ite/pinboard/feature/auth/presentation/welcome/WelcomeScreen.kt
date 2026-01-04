package kh.edu.rupp.fe.ite.pinboard.feature.auth.presentation.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.border
import androidx.compose.ui.tooling.preview.Preview
import kh.edu.rupp.fe.ite.pinboard.R

@Composable
fun WelcomeScreen(
    onSignUpClick: () -> Unit,
    onLogInClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Image Grid (3x3)
            ImageGrid()
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Pinterest Logo (Red circle with white P)
            PinterestLogo()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Welcome Text
            Text(
                text = "Welcome to Pinterest",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.weight(0.5f))
            
            // Sign Up Button
            Button(
                onClick = onSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE60023)
                )
            ) {
                Text(
                    text = "Sign up",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Log In Button (White with grey border)
            Button(
                onClick = onLogInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = "Log in",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF000000)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Terms and Privacy Text
            TermsAndPrivacyText()
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ImageGrid() {
    // Display banner.png as the image grid
    // If banner.png is a composite image with 9 images, it will display as a single image
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.banner),
            contentDescription = "Welcome banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
@Composable
fun PinterestLogo() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Color(0xFFE60023)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun TermsAndPrivacyText() {
    val annotatedString = buildAnnotatedString {
        append("By continuing, you agree to Pinterest's ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("Terms of Service")
        }
        append(" and acknowledge you've read our ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("Privacy Policy")
        }
    }
    
    Text(
        text = annotatedString,
        fontSize = 12.sp,
        color = Color(0xFF000000),
        textAlign = TextAlign.Center,
        lineHeight = 16.sp
    )
}

