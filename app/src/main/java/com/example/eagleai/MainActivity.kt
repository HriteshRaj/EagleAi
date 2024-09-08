package com.example.eagleai

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Mic

import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.eagleai.ui.theme.EagleAiTheme
import com.example.eagleai.ui.theme.appbg
import com.example.eagleai.ui.theme.chatcolor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


class MainActivity : ComponentActivity() {
    private val uriState = MutableStateFlow("")

    private lateinit var voiceToTextParser: VoiceToTextParser
    private lateinit var viewModel: ChatViewModel

    private val imagePicker =
        registerForActivityResult<PickVisualMediaRequest, Uri?>(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                uriState.update {
                    uri.toString()
                }

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel=ChatViewModel()
        voiceToTextParser = VoiceToTextParser( application,viewModel)

        setContent {
            var canRecord by remember{
                mutableStateOf(false)
            }
            val recordAudioLauncher= rememberLauncherForActivityResult(contract =
            ActivityResultContracts.RequestPermission(), onResult = {
                isGranted->
                canRecord=isGranted
            }
                )

            LaunchedEffect(key1=recordAudioLauncher){
                recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }


            
            EagleAiTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 30.dp),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Scaffold(
                        topBar = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()

                                    .height(45.dp)
                                    .padding(horizontal = 16.dp)

                            ) {

                            }
                        }
                    ) {
                        ChatScreen(paddingValues = it,
                            canRecord = canRecord,
                            voicetoTextParser = voiceToTextParser,
                           viewModel = viewModel)

                    }

                }
            }

            }
        }

  @SuppressLint("UnusedContentLambdaTargetStateParameter")
  @Composable
fun ChatScreen( paddingValues: PaddingValues,
                canRecord: Boolean,
                voicetoTextParser: VoiceToTextParser, viewModel: ChatViewModel){

    val chatViewModel= viewModel<ChatViewModel>()
      val chatState = chatViewModel.chatState.collectAsState().value
      val bitmap = getBitmap()

      Box(
          modifier = Modifier
              .fillMaxSize()
              .background(color = appbg)
      ) {
          Text(
              modifier = Modifier
                  .align(Alignment.TopCenter)
                  .padding(top = 30.dp),
              text = stringResource(id = R.string.app_name),
              fontSize = 30.sp, fontStyle = FontStyle.Normal,
              fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimary
          )


          if (chatState.chatList.isEmpty()) {
              // Show the image in the middle of the screen
              Image(
                  modifier = Modifier.align(Alignment.Center),
                 painter = painterResource(R.drawable.backgroundimg),
                  contentDescription = null
              )
          }
          Column(modifier= Modifier.fillMaxSize()
              , verticalArrangement = Arrangement.Bottom


          ) {
              LazyColumn(modifier= Modifier
                  .weight(1f)

                  .fillMaxWidth()
                  .padding(8.dp),
                  reverseLayout = true

              ) {

                  itemsIndexed(chatState.chatList){
                          index,chat->
                      if(chat.isFromUser){
                          UserChat(prompt = chat.prompt, bitmap = chat.bitmap)
                      }else{
                          ModelChat(response = chat.prompt)

                      }

                  }

              }

              Row (modifier= Modifier
                  .fillMaxWidth()
                  .padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically
              ){
                  Column {
                      bitmap?.let {
                          Image(modifier= Modifier
                              .size(40.dp)
                              .clip(RoundedCornerShape(12.dp))

                              .padding(bottom = 2.dp),
                              contentScale = ContentScale.Crop,
                              contentDescription = "pickedimage",bitmap=it.asImageBitmap())

                      }

                      Icon(

                          modifier= Modifier
                              .size(40.dp)
                              .clickable {
                                  imagePicker.launch(
                                      PickVisualMediaRequest
                                          .Builder()
                                          .setMediaType(
                                              ActivityResultContracts.PickVisualMedia.ImageOnly
                                          )
                                          .build()
                                  )
                              },

                          imageVector = Icons.Rounded.AddAPhoto, contentDescription ="add photo",
                          tint = Color.White)

                  }
                  Spacer(modifier = Modifier.height(8.dp))
                  TextField(modifier=Modifier.weight(1f),value =  chatState.prompt, onValueChange =  {
                      chatViewModel.onEvent(ChatEvent.UpdatePrompt(it))

                  }, placeholder = {
                      Text(text = "type to interact")
                  }

                  )
                  Icon(

                      modifier= Modifier
                          .size(40.dp)
                          .clickable {
                              chatViewModel.onEvent(ChatEvent.SendPrompt(chatState.prompt, bitmap))
                                uriState.update { "" }

                          },

                      imageVector = Icons.Filled.PlayCircle, contentDescription ="send chat",
                      tint = Color.White)

                  val state by voicetoTextParser.state.collectAsState()
                  
                AnimatedContent(targetState = state) {
                        voiceState ->
                    if (voiceState.isSpeaking) {
                        Icon(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    voicetoTextParser.stopListening()
                                },
                            imageVector = Icons.Rounded.Stop,
                            contentDescription = "stop listening",
                            tint = Color.Red
                        )

                    }
                    else {
                        Icon(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    if (canRecord) {
                                        voicetoTextParser.startListening()
                                    }
                                },
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "start listening",
                            tint = Color.White
                        )
                        LaunchedEffect(state.spokenText) {
                            if (state.spokenText.isNotEmpty()) {
                                chatViewModel.onVoiceInput(state.spokenText)
                            }
                        }
                    }

                }




              }



          }




      }

  }


    @Composable
    fun UserChat(prompt:String,bitmap: Bitmap?){

        Column(modifier=Modifier.padding(start=100.dp, bottom = 25.dp)){
            bitmap?.let {
                Image(modifier= Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .height(250.dp)
                    .padding(bottom = 2.dp),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,bitmap=it.asImageBitmap())

            }
            Text(
                modifier= Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(16.dp),


                text = prompt,
                fontSize = 20.sp,
                color = Color.Black)

        }

    }
    @Composable
    fun ModelChat(response:String){

        Column(modifier=Modifier.padding(end=70.dp, bottom = 25.dp)){

            Text(
                modifier= Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(chatcolor)
                    .padding(16.dp),


                text = response,
                fontSize = 20.sp,
                color = Color.White
            )

        }

    }
    @Composable
    private fun getBitmap(): Bitmap? {
        val uri = uriState.collectAsState().value

        val imageState: AsyncImagePainter.State = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .size(Size.ORIGINAL)
                .build()
        ).state

        if (imageState is AsyncImagePainter.State.Success) {
            return imageState.result.drawable.toBitmap()
        }

        return null
    }
}




