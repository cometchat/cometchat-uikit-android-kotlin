package com.cometchat.pro.uikit.ui_components.messages.threaded_message_list

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat
import com.cometchat.pro.core.CometChat.*
import com.cometchat.pro.core.MessagesRequest
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.*
import com.cometchat.pro.uikit.R
import com.cometchat.pro.uikit.ui_components.messages.extensions.Extensions
import com.cometchat.pro.uikit.ui_components.messages.extensions.collaborative.CometChatCollaborativeActivity
import com.cometchat.pro.uikit.ui_components.messages.forward_message.CometChatForwardMessageActivity
import com.cometchat.pro.uikit.ui_components.messages.live_reaction.LiveReactionListener
import com.cometchat.pro.uikit.ui_components.messages.live_reaction.ReactionClickListener
import com.cometchat.pro.uikit.ui_components.messages.message_actions.CometChatMessageActions
import com.cometchat.pro.uikit.ui_components.messages.message_actions.CometChatMessageActions.MessageActionListener
import com.cometchat.pro.uikit.ui_components.messages.message_actions.listener.MessageActionCloseListener
import com.cometchat.pro.uikit.ui_components.messages.message_actions.listener.OnMessageLongClick
import com.cometchat.pro.uikit.ui_components.messages.message_information.CometChatMessageInfoScreenActivity
import com.cometchat.pro.uikit.ui_components.messages.message_list.CometChatMessageList
import com.cometchat.pro.uikit.ui_components.messages.message_list.CometChatMessageListActivity
import com.cometchat.pro.uikit.ui_components.shared.cometchatAvatar.CometChatAvatar
import com.cometchat.pro.uikit.ui_components.shared.cometchatComposeBox.CometChatComposeBox
import com.cometchat.pro.uikit.ui_components.shared.cometchatComposeBox.listener.ComposeActionListener
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.CometChatReactionDialog
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.listener.OnReactionClickListener
import com.cometchat.pro.uikit.ui_components.shared.cometchatReaction.model.Reaction
import com.cometchat.pro.uikit.ui_components.shared.cometchatSmartReplies.CometChatSmartReply
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.ErrorMessagesUtils
import com.cometchat.pro.uikit.ui_resources.utils.FontUtils
import com.cometchat.pro.uikit.ui_resources.utils.MediaUtils
import com.cometchat.pro.uikit.ui_resources.utils.Utils
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import com.cometchat.pro.uikit.ui_resources.utils.sticker_header.StickyHeaderDecoration
import com.cometchat.pro.uikit.ui_settings.FeatureRestriction
import com.cometchat.pro.uikit.ui_settings.UIKitSettings
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.math.roundToInt


class CometChatThreadMessageList : Fragment(), View.OnClickListener, OnMessageLongClick, MessageActionCloseListener {
    private var isImageNotSafe: Boolean = false
    private var isDeleteMemberMessageVisible: Boolean = false
    private var isReactionsVisible: Boolean = false
    private var isReactionEnded: Boolean = true
    private lateinit var reactionLayout: ChipGroup
    private lateinit var addReaction: ImageView
    private lateinit var reactionInfo: HashMap<String, String>
    private var fontUtils: FontUtils? = null

    private var messageSentAt: Long = 0
    private var messageType: String? = null
    private var message: String? = null
    private var messageFileName: String? = null
    private var messageSize = 0
    private var messageMimeType: String? = null
    private var messageExtension: String? = null
    private var parentId = 0
    private var type: String? = null
    private var groupOwnerId: String? = null
    private var replyCount = 0
    private var Id: String? = null
    private var avatarUrl: String? = null
    private var name :String? = ""
    private var conversationName :String? = ""
    private var parentMessageCategory: String? = null
    private var parentMessageLatitude = 0.0
    private  var parentMessageLongitude = 0.0

    private var nestedScrollView: NestedScrollView? = null
    private var noReplyMessages: LinearLayout? = null
    private var ivForwardMessage: ImageView? = null
    private var ivMoreOption: ImageView? = null
    private var textMessage: TextView? = null
    private var imageMessage: ImageView? = null
    private var stickerMessage: ImageView? = null
    private var videoMessage: VideoView? = null
    private var fileMessage: RelativeLayout? = null
    private var locationMessage: RelativeLayout? = null
    private var mapView: ImageView? = null
    private var addressView: TextView? = null
    private var fileName: TextView? = null
    private var fileSize: TextView? = null
    private var fileExtension: TextView? = null
    private var bottomLayout: RelativeLayout? = null
    private var composeBox: CometChatComposeBox? = null
    private var messageShimmer: ShimmerFrameLayout? = null

    private var locationManager: LocationManager? = null
    private lateinit var locationListener: LocationListener
    private val location: Location? = null
    private var LATITUDE = 0.0
    private var LONGITUDE = 0.0
    private val MIN_TIME: Long = 1000
    private val MIN_DIST: Long = 5
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private val TAG = "CometChatThreadScreen"
    private val LIMIT = 30
    private var messagesRequest //Used to fetch messages.
            : MessagesRequest? = null
    private val mediaRecorder: MediaRecorder? = null
    private val mediaPlayer: MediaPlayer? = null
    private val audioFileNameWithPath: String? = null
    private var rvChatListView //Used to display list of messages.
            : RecyclerView? = null
    private var messageAdapter: ThreadAdapter? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var rvSmartReply: CometChatSmartReply? = null

    /**
     * **Avatar** is a UI Kit Component which is used to display user and group avatars.
     */
    private var tvName: TextView? = null
    private var tvTypingIndicator: TextView? = null
    private var senderAvatar: CometChatAvatar? = null
    private var senderName: TextView? = null
    private val tvSentAt: TextView? = null
    private var contxt: Context? = null
    private var blockUserLayout: LinearLayout? = null
    private var blockedUserName: TextView? = null
    private val stickyHeaderDecoration: StickyHeaderDecoration? = null
    private var toolbar: Toolbar? = null
    private var isBlockedByMe = false
    private var loggedInUserScope: String? = null
    private var editMessageLayout: RelativeLayout? = null
    private var tvMessageTitle: TextView? = null
    private var tvMessageSubTitle: TextView? = null
    private var replyMessageLayout: RelativeLayout? = null
    private var replyTitle: TextView? = null
    private var replyMessage: TextView? = null
    private var replyMedia: ImageView? = null
    private var replyClose: ImageView? = null
    private var baseMessage: BaseMessage? = null
    private var baseMessages: List<BaseMessage> = ArrayList()
    private val messageList: MutableList<BaseMessage> = ArrayList()
    private var isEdit = false
    private var isReply = false
    private val timer = Timer()
    private var typingTimer = Timer()
    private var isNoMoreMessages = false
    private val loggedInUser = getLoggedInUser()
    var CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var isInProgress = false
    private var isSmartReplyClicked = false
    private var onGoingCallView: RelativeLayout? = null
    private var onGoingCallTxt: TextView? = null
    private var onGoingCallClose: ImageView? = null
    var count = 0

    private var sentAt: TextView? = null
    private var tvReplyCount: TextView? = null
    private var isParent = true
    private var imageToFly: ImageView? = null
    private var liveReactionLayout: FrameLayout? = null

    private var whiteboardMessage: RelativeLayout? = null
    private var writeboardMessage: RelativeLayout? = null


    private lateinit var whiteBoardTxt: TextView
    private lateinit var writeBoardTxt: TextView
    private lateinit var joinWhiteBoard: MaterialButton
    private lateinit var joinWriteBoard: MaterialButton

    private var pollQuestion: String? = null
    private var pollOptions: String? = null
    private var pollResult: ArrayList<String>? = null
    private var voteCount = 0

    private var pollMessage: LinearLayout? = null
    private var pollQuestionTv: TextView? = null
    private var pollOptionsLL: LinearLayout? = null
    private var totalCount: TextView? = null

    private var ivBackArrow: ImageView? = null

//    private var view: View? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleArguments()
        if (activity != null) fontUtils = FontUtils.getInstance(activity)
    }

    private fun handleArguments() {
        if (arguments != null) {
            parentId = arguments!!.getInt(UIKitConstants.IntentStrings.PARENT_ID, 0)
            replyCount = arguments!!.getInt(UIKitConstants.IntentStrings.REPLY_COUNT, 0)
            type = arguments!!.getString(UIKitConstants.IntentStrings.TYPE)
            Id = arguments!!.getString(UIKitConstants.IntentStrings.ID)
            avatarUrl = arguments!!.getString(UIKitConstants.IntentStrings.AVATAR)
            name = arguments!!.getString(UIKitConstants.IntentStrings.NAME)
            conversationName = arguments!!.getString(UIKitConstants.IntentStrings.CONVERSATION_NAME)
            messageType = arguments!!.getString(UIKitConstants.IntentStrings.MESSAGE_TYPE)
            messageSentAt = arguments!!.getLong(UIKitConstants.IntentStrings.SENTAT)
            parentMessageCategory = arguments!!.getString(UIKitConstants.IntentStrings.MESSAGE_CATEGORY)
            if (messageType == CometChatConstants.MESSAGE_TYPE_TEXT) {
                message = arguments!!.getString(UIKitConstants.IntentStrings.TEXTMESSAGE)
            } else if (messageType == UIKitConstants.IntentStrings.LOCATION) {
                parentMessageLatitude = arguments!!.getDouble(UIKitConstants.IntentStrings.LOCATION_LATITUDE)
                parentMessageLongitude = arguments!!.getDouble(UIKitConstants.IntentStrings.LOCATION_LONGITUDE)
            } else if (messageType == UIKitConstants.IntentStrings.STICKERS) {
                message = arguments!!.getString(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL)
                messageFileName = arguments!!.getString(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME)
            }
            else if (messageType == UIKitConstants.IntentStrings.WHITEBOARD || messageType == UIKitConstants.IntentStrings.WRITEBOARD) {
                message = arguments!!.getString(UIKitConstants.IntentStrings.TEXTMESSAGE)
            } else if (messageType == UIKitConstants.IntentStrings.POLLS) {
                pollQuestion = arguments!!.getString(UIKitConstants.IntentStrings.POLL_QUESTION)
                pollOptions = arguments!!.getString(UIKitConstants.IntentStrings.POLL_OPTION)
                pollResult = arguments!!.getStringArrayList(UIKitConstants.IntentStrings.POLL_RESULT)
                voteCount = arguments!!.getInt(UIKitConstants.IntentStrings.POLL_VOTE_COUNT)
            }
            else {
                message = arguments!!.getString(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL)
                messageFileName = arguments!!.getString(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME)
                messageExtension = arguments!!.getString(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION)
                messageSize = arguments!!.getInt(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, 0)
                messageMimeType = arguments!!.getString(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE)
            }
                reactionInfo = (arguments!!.getSerializable(UIKitConstants.IntentStrings.REACTION_INFO)) as HashMap<String, String>

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment

        // Inflate the layout for this fragment
        var view:View = inflater.inflate(R.layout.fragment_cometchat_thread_message, container, false)
        initViewComponent(view)
        return view
    }

    private fun initViewComponent(view: View) {
        setHasOptionsMenu(true)
        nestedScrollView = view.findViewById(R.id.nested_scrollview)
        noReplyMessages = view.findViewById<LinearLayout>(R.id.no_reply_layout)
        ivMoreOption = view.findViewById<ImageView>(R.id.ic_more_option)
        ivMoreOption!!.setOnClickListener(this)
        ivForwardMessage = view.findViewById<ImageView>(R.id.ic_forward_option)
        ivForwardMessage!!.setOnClickListener(this)
        textMessage = view.findViewById<TextView>(R.id.tv_textMessage)
        imageMessage = view.findViewById<ImageView>(R.id.iv_imageMessage)
        videoMessage = view.findViewById<VideoView>(R.id.vv_videoMessage)
        fileMessage = view.findViewById<RelativeLayout>(R.id.rl_fileMessage)
        locationMessage = view.findViewById<RelativeLayout>(R.id.rl_locationMessage)
        mapView = view.findViewById<ImageView>(R.id.iv_mapView)
        addressView = view.findViewById<TextView>(R.id.tv_address)
        fileName = view.findViewById<TextView>(R.id.tvFileName)
        fileSize = view.findViewById<TextView>(R.id.tvFileSize)
        fileExtension = view.findViewById<TextView>(R.id.tvFileExtension)
        stickerMessage = view.findViewById(R.id.iv_stickerMessage)

        whiteboardMessage = view.findViewById(R.id.whiteboard_vw)
        whiteBoardTxt = view.findViewById(R.id.whiteboard_message)
        joinWhiteBoard = view.findViewById(R.id.join_whiteboard)
        writeboardMessage = view.findViewById(R.id.writeboard_vw)
        writeBoardTxt = view.findViewById(R.id.writeboard_message)
        joinWriteBoard = view.findViewById(R.id.join_writeboard)

        pollMessage = view.findViewById(R.id.polls_message)
        pollQuestionTv = view.findViewById(R.id.tv_question)
        pollOptionsLL = view.findViewById(R.id.options_group)
        totalCount = view.findViewById(R.id.total_votes)

        ivBackArrow = view.findViewById(R.id.iv_back_arrow)
        ivBackArrow?.setOnClickListener {
            activity?.onBackPressed()
        }
        if (messageType == CometChatConstants.MESSAGE_TYPE_IMAGE) {
            imageMessage!!.visibility = View.VISIBLE
            Glide.with(context!!).load(message).into(imageMessage!!)
        } else if (messageType == CometChatConstants.MESSAGE_TYPE_VIDEO) {
            videoMessage!!.visibility = View.VISIBLE
            val mediacontroller = MediaController(context)
            mediacontroller.setAnchorView(videoMessage)
            videoMessage!!.setMediaController(mediacontroller)
            videoMessage!!.setVideoURI(Uri.parse(message))
        } else if (messageType == CometChatConstants.MESSAGE_TYPE_FILE || messageType == CometChatConstants.MESSAGE_TYPE_AUDIO) {
            fileMessage!!.visibility = View.VISIBLE
            if (messageFileName != null) fileName!!.text = messageFileName
            if (messageExtension != null) fileExtension!!.text = messageExtension
            fileSize!!.text = Utils.getFileSize(messageSize)
        } else if (messageType == CometChatConstants.MESSAGE_TYPE_TEXT) {
            textMessage!!.visibility = View.VISIBLE
            textMessage!!.text = message
        } else if (messageType == UIKitConstants.IntentStrings.STICKERS) {
            ivForwardMessage!!.visibility = View.GONE
            stickerMessage?.visibility = View.VISIBLE
            Glide.with(context!!).load(message).into(stickerMessage!!)
        } else if (messageType == UIKitConstants.IntentStrings.WHITEBOARD) {
            ivForwardMessage?.visibility = View.GONE
            whiteboardMessage?.visibility = View.VISIBLE
            if (name == loggedInUser.name) whiteBoardTxt.text = getString(R.string.you_created_whiteboard) else whiteBoardTxt.text = name + " " + getString(R.string.has_shared_whiteboard)
            joinWhiteBoard.setOnClickListener {
                val boardUrl = message!!
                val intent = Intent(context, CometChatCollaborativeActivity::class.java)
                intent.putExtra(UIKitConstants.IntentStrings.URL, boardUrl)
                startActivity(intent)
            }
        } else if (messageType == UIKitConstants.IntentStrings.WRITEBOARD) {
            ivForwardMessage?.visibility = View.GONE
            writeboardMessage?.visibility = View.VISIBLE
            if (name == loggedInUser.name) writeBoardTxt.text = getString(R.string.you_created_document) else writeBoardTxt.text = name + " " + getString(R.string.has_shared_document)
            joinWriteBoard.setOnClickListener {
                val boardUrl = message!!
                val intent = Intent(context, CometChatCollaborativeActivity::class.java)
                intent.putExtra(UIKitConstants.IntentStrings.URL, boardUrl)
                startActivity(intent)
            }
        }
        else if (messageType == UIKitConstants.IntentStrings.LOCATION) {
            initLocation()
            locationMessage!!.visibility = View.VISIBLE
            addressView!!.text = Utils.getAddress(context, parentMessageLatitude, parentMessageLongitude)
            val mapUrl = UIKitConstants.MapUrl.MAPS_URL + parentMessageLatitude + "," + parentMessageLongitude + "&key=" + UIKitConstants.MapUrl.MAP_ACCESS_KEY
            Glide.with(context!!)
                    .load(mapUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mapView!!)
        } else if (messageType == UIKitConstants.IntentStrings.POLLS) {
            ivForwardMessage!!.visibility = View.GONE
            pollMessage?.visibility = View.VISIBLE
            totalCount?.text = "$voteCount Votes"
            pollQuestionTv?.text = pollQuestion
            try {
                val options = JSONObject(pollOptions)
                val voterInfo = pollResult!!
                for (k in 0 until options.length()) {
                    val linearLayout = LinearLayout(context)
                    val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT)
                    linearLayout.setPadding(8, 8, 8, 8)
                    linearLayout.background = context!!.resources
                            .getDrawable(R.drawable.cc_message_bubble_right)
                    linearLayout.backgroundTintList = ColorStateList.valueOf(context!!.resources
                            .getColor(R.color.textColorWhite))
                    layoutParams.bottomMargin = Utils.dpToPx(context!!, 8F).toInt()
                    linearLayout.layoutParams = layoutParams
                    val textViewPercentage = TextView(context)
                    val textViewOption = TextView(context)
                    textViewPercentage.setPadding(16, 4, 0, 4)
                    textViewOption.setPadding(16, 4, 0, 4)
                    textViewOption.setTextAppearance(context, androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
                    textViewPercentage.setTextAppearance(context, androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
                    textViewPercentage.setTextColor(context!!.getColor(R.color.primaryTextColor))
                    textViewOption.setTextColor(context!!.resources.getColor(R.color.primaryTextColor))
                    val optionStr = options.getString((k + 1).toString())
                    if (voteCount > 0) {
                        val percentage = (voterInfo[k].toInt() * 100 /
                                voteCount.toFloat()).roundToInt()
                        if (percentage > 0) textViewPercentage.text = "$percentage% "
                    }
                    textViewOption.text = optionStr
                    if (pollOptionsLL?.childCount != options.length()) {
                        linearLayout.addView(textViewPercentage)
                        linearLayout.addView(textViewOption)
                        pollOptionsLL?.addView(linearLayout)
                    }
                    textViewOption.setOnClickListener {
                        try {
                            val jsonObject = JSONObject()
                            jsonObject.put("vote", k + 1)
                            jsonObject.put("id", baseMessage!!.id)
                            callExtension("polls", "POST", "/v1/vote",
                                    jsonObject, object : CallbackListener<JSONObject>() {
                                override fun onSuccess(jsonObject: JSONObject) {
                                    // Voted successfully
                                    Log.e(CometChatThreadMessageList.TAG, "onSuccess: $jsonObject")
                                    Toast.makeText(context, "Voted Successfully", Toast.LENGTH_LONG).show()
                                }

                                override fun onError(e: CometChatException) {
                                    // Some error occured
                                    ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                                    Log.e(CometChatThreadMessageList.TAG, "onErrorExtension: ${e.message}${e.code}".trimIndent())
                                }
                            })
                        } catch (e: Exception) {
                            Log.e(CometChatThreadMessageList.TAG, "onError: " + e.message)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(CometChatThreadMessageList.TAG, "setPollsData: " + e.message)
            }
        }
        bottomLayout = view.findViewById(R.id.bottom_layout)
        composeBox = view.findViewById(R.id.message_box)
        messageShimmer = view.findViewById(R.id.shimmer_layout)
        composeBox?.usedIn(CometChatThreadMessageListActivity::class.java.name)

        composeBox?.hidePollOption(true)
        composeBox?.hideStickerOption(true)
        composeBox?.hideWriteBoardOption(true)
        composeBox?.hideWhiteBoardOption(true)
//        composeBox?.hideGroupCallOption(true)
        composeBox?.hideRecordOption(true)
        composeBox?.hideSendButton(false)
//        composeBox?.ivMic?.visibility = View.GONE
        FeatureRestriction.isOneOnOneChatEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                if (p0) composeBox?.visibility = View.VISIBLE else composeBox?.visibility = View.GONE
            }
        })

        liveReactionLayout = view.findViewById(R.id.live_reactions_layout)
        composeBox?.btnLiveReaction?.setOnClickListener {
            if (isReactionEnded)
                sendLiveReaction()
        }

        addReaction = view.findViewById(R.id.add_reaction)
        reactionLayout = view.findViewById(R.id.reactions_layout)
        if (reactionInfo.size > 0) reactionLayout.visibility = View.VISIBLE
        setReactionForParentMessage()
        addReaction.setOnClickListener {
            val reactionDialog = CometChatReactionDialog()
            reactionDialog.setOnEmojiClick(object : OnReactionClickListener {
                override fun onEmojiClicked(emojicon: Reaction) {
                    val body = JSONObject()
                    try {
                        body.put("msgId", parentId)
                        body.put("emoji", emojicon.name)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    callExtension("reactions", "POST", "/v1/react", body,
                            object : CallbackListener<JSONObject>() {
                                override fun onSuccess(responseObject: JSONObject) {
                                    reactionLayout.visibility = View.VISIBLE
                                    reactionDialog.dismiss()
                                    Log.e(Companion.TAG, "onSuccess: $responseObject")
                                    // ReactionModel added successfully.
                                }

                                override fun onError(e: CometChatException) {
                                    ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                                    // Some error occured.
                                }
                            })
                }
            })
            reactionDialog.show(fragmentManager!!, "ReactionThreadDialog")
        }


        setComposeBoxListener()

        rvSmartReply = view.findViewById(R.id.rv_smartReply)
        FeatureRestriction.isSmartRepliesEnabled(object : FeatureRestriction.OnSuccessListener{
            override fun onSuccess(p0: Boolean) {
                if (!p0)
                    rvSmartReply?.visibility = View.GONE
            }
        })
        editMessageLayout = view.findViewById<RelativeLayout>(R.id.editMessageLayout)
        tvMessageTitle = view.findViewById<TextView>(R.id.tv_message_layout_title)
        tvMessageSubTitle = view.findViewById<TextView>(R.id.tv_message_layout_subtitle)
        val ivMessageClose = view.findViewById<ImageView>(R.id.iv_message_close)
        ivMessageClose.setOnClickListener(this)

        replyMessageLayout = view.findViewById<RelativeLayout>(R.id.replyMessageLayout)
        replyTitle = view.findViewById<TextView>(R.id.tv_reply_layout_title)
        replyMessage = view.findViewById<TextView>(R.id.tv_reply_layout_subtitle)
        replyMedia = view.findViewById<ImageView>(R.id.iv_reply_media)
        replyClose = view.findViewById<ImageView>(R.id.iv_reply_close)
        replyClose!!.setOnClickListener(this)

        senderAvatar = view.findViewById(R.id.av_sender)
        setAvatar()
        senderName = view.findViewById<TextView>(R.id.tv_sender_name)
        senderName!!.text = name
        sentAt = view.findViewById(R.id.tv_message_time)
        sentAt!!.text = String.format(getString(R.string.sentattxt), Utils.getMessageDate(messageSentAt))
        tvReplyCount = view.findViewById<TextView>(R.id.thread_reply_count)
        rvChatListView = view.findViewById(R.id.rv_message_list)
        if (parentMessageCategory == CometChatConstants.CATEGORY_CUSTOM) ivMoreOption!!.visibility = View.GONE
        if (replyCount > 0) {
            tvReplyCount!!.text = "$replyCount Replies"
            noReplyMessages!!.visibility = View.GONE
        } else {
            noReplyMessages!!.visibility = View.VISIBLE
        }

        val unblockUserBtn: MaterialButton = view.findViewById(R.id.btn_unblock_user)
        unblockUserBtn.setOnClickListener(this)
        blockedUserName = view.findViewById<TextView>(R.id.tv_blocked_user_name)
        blockUserLayout = view.findViewById<LinearLayout>(R.id.blocked_user_layout)
        tvName = view.findViewById<TextView>(R.id.tv_name)
        tvTypingIndicator = view.findViewById<TextView>(R.id.tv_typing)
        toolbar = view.findViewById(R.id.chatList_toolbar)
//        toolbar!!.setOnClickListener(this)
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        tvName!!.typeface = fontUtils!!.getTypeFace(FontUtils.robotoMedium)
        tvName!!.text = String.format(getString(R.string.thread_in_name), conversationName)
        setAvatar()
        rvChatListView!!.layoutManager = linearLayoutManager

//        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
//        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        if (Utils.isDarkMode(context!!)) {
            ivMoreOption!!.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            ivForwardMessage!!.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            bottomLayout!!.setBackgroundColor(resources.getColor(R.color.darkModeBackground))
            toolbar!!.setBackgroundColor(resources.getColor(R.color.grey))
            editMessageLayout!!.background = resources.getDrawable(R.drawable.left_border_dark)
            replyMessageLayout!!.background = resources.getDrawable(R.drawable.left_border_dark)
            composeBox!!.setBackgroundColor(resources.getColor(R.color.darkModeBackground))
            rvChatListView!!.setBackgroundColor(resources.getColor(R.color.darkModeBackground))
            tvName!!.setTextColor(resources.getColor(R.color.textColorWhite))
        } else {
            ivMoreOption!!.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryTextColor))
            ivForwardMessage!!.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryTextColor))
            bottomLayout!!.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textColorWhite))
            toolbar!!.setBackgroundColor(resources.getColor(R.color.textColorWhite))
            editMessageLayout!!.background = resources.getDrawable(R.drawable.left_border)
            replyMessageLayout!!.background = resources.getDrawable(R.drawable.left_border)
            composeBox!!.setBackgroundColor(resources.getColor(R.color.textColorWhite))
            rvChatListView!!.setBackgroundColor(resources.getColor(R.color.textColorWhite))
            tvName!!.setTextColor(resources.getColor(R.color.primaryTextColor))
        }


        // Uses to fetch next list of messages if rvChatListView (RecyclerView) is scrolled in downward direction.
        rvChatListView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

                //for toolbar elevation animation i.e stateListAnimator
                toolbar!!.isSelected = rvChatListView!!.canScrollVertically(-1)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isNoMoreMessages && !isInProgress) {
                    if (linearLayoutManager!!.findFirstVisibleItemPosition() == 10 || !rvChatListView!!.canScrollVertically(-1)) {
                        isInProgress = true
                        fetchMessage()
                    }
                }
            }
        })
        rvSmartReply!!.setItemClickListener(object : OnItemClickListener<String?>() {
            override fun OnItemClick(t: Any, position: Int) {
                if (!isSmartReplyClicked) {
                    isSmartReplyClicked = true
                    rvSmartReply!!.visibility = View.GONE
                    sendMessage(t as String)
                }
            }
        })

        //Check Ongoing Call

        //Check Ongoing Call
        onGoingCallView = view.findViewById<RelativeLayout>(R.id.ongoing_call_view)
        onGoingCallClose = view.findViewById<ImageView>(R.id.close_ongoing_view)
        onGoingCallTxt = view.findViewById<TextView>(R.id.ongoing_call)
        checkOnGoingCall()
    }

    private fun setReactionForParentMessage() {
        for ((k, v) in reactionInfo) {
            val chip = Chip(context)
            chip.chipStrokeWidth = 2f
            chip.chipBackgroundColor = ColorStateList.valueOf(context!!.resources.getColor(android.R.color.transparent))
            chip.chipStrokeColor = ColorStateList.valueOf(Color.parseColor(UIKitSettings.color))
//            chip.chipStrokeColor = ColorStateList.valueOf(context!!.resources.getColor(R.color.colorPrimaryDark))
            chip.text = k + " " + reactionInfo[k]
            reactionLayout.addView(chip)
            chip.setOnClickListener {
                val body = JSONObject()
                try {
                    body.put("msgId", parentId)
                    body.put("emoji", k)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                callExtension("reactions", "POST", "/v1/react", body,
                        object : CallbackListener<JSONObject>() {
                            override fun onSuccess(responseObject: JSONObject) {
                                Log.e(Companion.TAG, "onSuccess: $responseObject")
                                // ReactionModel added successfully.
                            }

                            override fun onError(e: CometChatException) {
                                // Some error occured.
                            }
                        })
            }
        }
    }

    private fun sendLiveReaction() {
        var metadata : JSONObject = JSONObject()
        metadata.put("type","live_reaction")
        metadata.put("reaction","heart")
        val transientMessage = TransientMessage(Id, type, metadata)
        sendTransientMessage(transientMessage)
        setReaction()
    }

    private fun setReaction() {
        isReactionEnded = false
        for (i in 1..5) {
            val imageView = ImageView(context)

            var layoutParam: FrameLayout.LayoutParams  = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParam.gravity = Gravity.BOTTOM or Gravity.END
            liveReactionLayout?.alpha = 1.0f

            if (i.rem(2) == 0){
                layoutParam.rightMargin = 16
                imageView.layoutParams = layoutParam
                liveReactionLayout?.addView(imageView)
            } else if (i.rem(3) == 0){
                layoutParam.rightMargin = 30
                imageView.layoutParams = layoutParam
                liveReactionLayout?.addView(imageView)
            } else {
                layoutParam.rightMargin = 10
                imageView.layoutParams = layoutParam
                liveReactionLayout?.addView(imageView)
            }

            val bitmap = BitmapFactory.decodeResource(context?.resources, R.drawable.heart_reaction)
            if (bitmap != null) {
                val scaledBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * 0.2f).toInt(),
                    (bitmap.height * 0.2f).toInt(),
                    false
                )
                imageView.setImageBitmap(scaledBitmap)
            }

            var transition: ObjectAnimator = ObjectAnimator.ofFloat(imageView, "translationY", -400f)
            var fadeOut: ObjectAnimator = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f)
            transition.repeatCount = 3
            fadeOut.repeatCount = 3

            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    imageView.visibility = View.GONE
                }
            })

            if (i.rem(2) == 0) {
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(transition, fadeOut)
                animatorSet.duration = 500L
                animatorSet.interpolator = AccelerateDecelerateInterpolator()
                animatorSet.start()
            }
            else if (i.rem(3)== 0){
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(transition, fadeOut)
                animatorSet.duration = 500L
                animatorSet.startDelay = 400L
                animatorSet.interpolator = AccelerateDecelerateInterpolator()
                animatorSet.start()
            } else {

                val animatorSet = AnimatorSet()
                animatorSet.playTogether(transition, fadeOut)
                animatorSet.duration = 500L
                animatorSet.startDelay = 800L
                animatorSet.interpolator = AccelerateDecelerateInterpolator()
                animatorSet.start()
            }
            Handler().postDelayed({
                isReactionEnded = true
            },1500)
        }
    }

    private fun checkOnGoingCall() {
        if (getActiveCall() != null && getActiveCall().callStatus == CometChatConstants.CALL_STATUS_ONGOING && getActiveCall().sessionId != null) {
            if (onGoingCallView != null) onGoingCallView!!.visibility = View.VISIBLE
            if (onGoingCallTxt != null) {
                onGoingCallTxt!!.setOnClickListener(View.OnClickListener {
                    onGoingCallView!!.visibility = View.GONE
                    Utils.joinOnGoingCall(context!!)
                })
            }
            if (onGoingCallClose != null) {
                onGoingCallClose!!.setOnClickListener(View.OnClickListener { onGoingCallView!!.visibility = View.GONE })
            }
        } else if (getActiveCall() != null) {
            if (onGoingCallView != null) onGoingCallView!!.visibility = View.GONE
            Log.e(Companion.TAG, "checkOnGoingCall: " + getActiveCall().toString())
        }
    }

    private fun setComposeBoxListener() {

        composeBox!!.setComposeBoxListener(object : ComposeActionListener() {

            override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
                if (charSequence!!.isNotEmpty()) {
                    sendTypingIndicator(false)
                } else {
                    sendTypingIndicator(true)
                }
            }

            override fun afterTextChanged(editable: Editable?) {
                if (typingTimer == null) {
                    typingTimer = Timer()
                }
                endTypingTimer()
            }

            override fun onAudioActionClicked() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    if (Utils.hasPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        MediaUtils.openAudio(
                            activity!!
                        )
                    } else {
                        requestPermissions(
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            UIKitConstants.RequestCode.AUDIO
                        )
                    }
                } else {
                    if (Utils.hasPermissions(context, Manifest.permission.READ_MEDIA_AUDIO)) {
                        MediaUtils.openAudio(
                            activity!!
                        )

                    } else {
                        requestPermissions(
                            arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                            UIKitConstants.RequestCode.AUDIO
                        )
                    }
                }
            }

            override fun onCameraActionClicked() {
                if (Utils.hasPermissions(context, *CAMERA_PERMISSION)) {
                    startActivityForResult(MediaUtils.openCamera(context!!), UIKitConstants.RequestCode.CAMERA)
                } else {
                    requestPermissions(CAMERA_PERMISSION, UIKitConstants.RequestCode.CAMERA)
                }
            }

            override fun onGalleryActionClicked() {
                if (Utils.hasPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    MediaUtils.openGallery(activity!!)
                } else {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), UIKitConstants.RequestCode.GALLERY)
                }
            }

            override fun onFileActionClicked() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU || Utils.hasPermissions(
                        context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    startActivityForResult(
                        MediaUtils.getFileIntent(UIKitConstants.IntentStrings.EXTRA_MIME_DOC),
                        UIKitConstants.RequestCode.FILE
                    )
                } else {
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        UIKitConstants.RequestCode.FILE
                    )
                }
            }

            override fun onSendActionClicked(editText: EditText?) {
                val message = editText!!.text.toString().trim { it <= ' ' }
                editText.setText("")
                editText.hint = getString(R.string.message)
                if (isEdit) {
                    if (isParent) editThread(message) else {
                        editMessage(baseMessage, message)
                    }
                    editMessageLayout!!.visibility = View.GONE
                } else if (isReply) {
                    replyMessage(baseMessage, message)
                    replyMessageLayout!!.visibility = View.GONE
                } else if (message.isNotEmpty()) sendMessage(message)
            }

            override fun onLocationActionClicked() {
                if (Utils.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    initLocation()
                    val provider = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    if (!provider) {
                        turnOnLocation()
                    } else {
                        getLocation()
                    }
                } else {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), UIKitConstants.RequestCode.LOCATION)
                }
            }


        })
    }

    private fun getLocation() {
        fusedLocationProviderClient!!.lastLocation.addOnSuccessListener(object : OnSuccessListener<Location?> {
            override fun onSuccess(location: Location?) {
                if (location != null) {
                    val lon = location.longitude
                    val lat = location.latitude
                    val customData = JSONObject()
                    try {
                        customData.put("latitude", lat)
                        customData.put("longitude", lon)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    initAlert(customData)
                }
            }
        })
    }

    private fun initAlert(customData: JSONObject) {
        val builder = AlertDialog.Builder(context!!)
        val view = LayoutInflater.from(context).inflate(R.layout.map_share_layout, null)
        builder.setView(view)
        try {
            LATITUDE = customData.getDouble("latitude")
            LONGITUDE = customData.getDouble("longitude")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val address = view.findViewById<TextView>(R.id.address)
        address.text = "Address: " + Utils.getAddress(context, LATITUDE, LONGITUDE)
        val mapView = view.findViewById<ImageView>(R.id.map_vw)
        val mapUrl = UIKitConstants.MapUrl.MAPS_URL + LATITUDE + "," + LONGITUDE + "&key=" +
                UIKitConstants.MapUrl.MAP_ACCESS_KEY
        Glide.with(this)
                .load(mapUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mapView)
        builder.setPositiveButton(getString(R.string.share)) { dialog, which -> sendCustomMessage("LOCATION", customData) }.setNegativeButton(getString(R.string.no)) { dialog, which -> dialog.dismiss() }
        builder.create()
        builder.show()
    }

    private fun sendCustomMessage(customType: String, customData: JSONObject) {
        val customMessage: CustomMessage
        customMessage = if (type.equals(CometChatConstants.RECEIVER_TYPE_USER, ignoreCase = true)) CustomMessage(Id, CometChatConstants.RECEIVER_TYPE_USER, customType, customData) else CustomMessage(Id, CometChatConstants.RECEIVER_TYPE_GROUP, customType, customData)
        customMessage.parentMessageId = parentId
        sendCustomMessage(customMessage, object : CallbackListener<CustomMessage?>() {
            override fun onSuccess(customMessage: CustomMessage?) {
                noReplyMessages!!.visibility = View.GONE
                if (messageAdapter != null) {
                    messageAdapter!!.addMessage(customMessage)
                    setReply()
                    scrollToBottom()
                }
            }

            override fun onError(e: CometChatException) {
                if (activity != null) {
                    ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                }
            }
        })
    }

    private fun turnOnLocation() {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Turn on GPS")
        builder.setPositiveButton("ON") { dialog, which -> startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), UIKitConstants.RequestCode.LOCATION) }.setNegativeButton("Close") { dialog, which -> dialog.dismiss() }
        builder.create()
        builder.show()
    }

    private fun initLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(activity!!))
        locationManager = Objects.requireNonNull(context!!).getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (Utils.hasPermissions(context, *arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))) {
            try {
                locationListener.let { locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST.toFloat(), it) }
                locationListener.let { locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST.toFloat(), it) }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), UIKitConstants.RequestCode.LOCATION)
        }
    }

    private fun editThread(editMessage: String) {
        isEdit = false
        val textmessage: TextMessage
        textmessage = if (type.equals(CometChatConstants.RECEIVER_TYPE_USER, ignoreCase = true)) TextMessage(Id!!, editMessage, CometChatConstants.RECEIVER_TYPE_USER) else TextMessage(Id!!, editMessage, CometChatConstants.RECEIVER_TYPE_GROUP)
        sendTypingIndicator(true)
        textmessage.id = parentId
        editMessage(textmessage, object : CallbackListener<BaseMessage>() {
            override fun onSuccess(baseMessage: BaseMessage) {
                textMessage!!.text = (baseMessage as TextMessage).text
                message = baseMessage.text
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                Log.d(Companion.TAG, "onError: " + e.message)
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        Log.d(Companion.TAG, "onRequestPermissionsResult: ")
        when (requestCode) {
            UIKitConstants.RequestCode.CAMERA -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) MediaUtils.openCamera(activity!!)else showSnackBar(view!!.findViewById<View>(R.id.message_box), resources.getString(R.string.grant_camera_permission))
            UIKitConstants.RequestCode.GALLERY -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) MediaUtils.openGallery(activity!!) else showSnackBar(view!!.findViewById<View>(R.id.message_box), resources.getString(R.string.grant_storage_permission))
            UIKitConstants.RequestCode.FILE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) startActivityForResult(MediaUtils.getFileIntent(UIKitConstants.IntentStrings.EXTRA_MIME_DOC), UIKitConstants.RequestCode.FILE) else showSnackBar(view!!.findViewById<View>(R.id.message_box), resources.getString(R.string.grant_storage_permission))
            UIKitConstants.RequestCode.LOCATION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else showSnackBar(view!!.findViewById<View>(R.id.message_box), resources.getString(R.string.grant_location_permission))
        }
    }

    private fun showSnackBar(view: View, message: String) {
        ErrorMessagesUtils.showCometChatErrorDialog(context, message)
    }

    private fun unblockUser() {
        val uids = ArrayList<String>()
        uids.add(Id!!)
        unblockUsers(uids, object : CallbackListener<HashMap<String?, String?>?>() {
            override fun onSuccess(stringStringHashMap: HashMap<String?, String?>?) {
                Snackbar.make(rvChatListView!!, String.format(resources.getString(R.string.unblocked_successfully), name), Snackbar.LENGTH_LONG).show()
                blockUserLayout!!.visibility = View.GONE
                isBlockedByMe = false
                messagesRequest = null
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
            }
        })
    }

    private fun fetchMessage() {
        if (messagesRequest == null) {
            messagesRequest = MessagesRequest.MessagesRequestBuilder().setLimit(Companion.LIMIT).setParentMessageId(parentId).hideMessagesFromBlockedUsers(true).build()
        }
        messagesRequest!!.fetchPrevious(object : CallbackListener<List<BaseMessage?>>() {
            override fun onSuccess(baseMessages: List<BaseMessage?>) {
                isInProgress = false
                val filteredMessageList: List<BaseMessage> = filterBaseMessages(baseMessages)
                initMessageAdapter(filteredMessageList)
                if (baseMessages.isNotEmpty()) {
                    stopHideShimmer()
                    val baseMessage = baseMessages[baseMessages.size - 1]
                    if (baseMessage != null) {
                        markAsRead(baseMessage)
                    }
                }
                if (baseMessages.isEmpty()) {
                    stopHideShimmer()
                    isNoMoreMessages = true
                }
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                Log.d(Companion.TAG, "onError: " + e.message)
            }
        })
    }

    private fun stopHideShimmer() {
        messageShimmer!!.stopShimmer()
        messageShimmer!!.visibility = View.GONE
    }

    private fun filterBaseMessages(baseMessages: List<BaseMessage?>): List<BaseMessage> {
        val tempList: MutableList<BaseMessage> = ArrayList()
        for (baseMessage in baseMessages) {
            Log.e(Companion.TAG, "filterBaseMessages: " + baseMessage!!.sentAt)
            if (baseMessage.category == CometChatConstants.CATEGORY_ACTION) {
                val action = baseMessage as Action
                if (action.action == CometChatConstants.ActionKeys.ACTION_MESSAGE_DELETED || action.action == CometChatConstants.ActionKeys.ACTION_MESSAGE_EDITED) {
                } else {
                    tempList.add(baseMessage)
                }
            } else {
                tempList.add(baseMessage)
            }
        }
        return tempList
    }

    private fun getSmartReplyList(baseMessage: BaseMessage) {
//        val extensionList: HashMap<String, JSONObject> = Extensions.extensionCheck(baseMessage)
        val extensionList = Extensions.extensionCheck(baseMessage)
        if (extensionList != null && extensionList.containsKey("smartReply")) {
            rvSmartReply!!.visibility = View.VISIBLE
            val replyObject = extensionList["smartReply"]
            val replyList: MutableList<String> = ArrayList()
            try {
                replyList.add(replyObject!!.getString("reply_positive"))
                replyList.add(replyObject.getString("reply_neutral"))
                replyList.add(replyObject.getString("reply_negative"))
            } catch (e: Exception) {
                Log.e(TAG, "onSuccess: " + e.message)
            }
            setSmartReplyAdapter(replyList)
        } else {
            rvSmartReply!!.visibility = View.GONE
        }
    }

    private fun setSmartReplyAdapter(replyList: List<String>) {
        rvSmartReply!!.setSmartReplyList(replyList)
        scrollToBottom()
    }

    private fun initMessageAdapter(messageList: List<BaseMessage>) {
        if (messageAdapter == null) {
            messageAdapter = ThreadAdapter(activity!!, messageList, type!!)
            rvChatListView!!.adapter = messageAdapter
            messageAdapter!!.notifyDataSetChanged()
        } else {
            messageAdapter!!.updateList(messageList)
        }
        if (!isBlockedByMe && rvSmartReply!!.adapter!!.itemCount == 0 && rvSmartReply!!.visibility == View.GONE) {
            val lastMessage: BaseMessage? = messageAdapter!!.getLastMessage()
            checkSmartReply(lastMessage)
        }
    }

    private fun sendTypingIndicator(isEnd: Boolean) {
        if (isEnd) {
            if (type == CometChatConstants.RECEIVER_TYPE_USER) {
                endTyping(TypingIndicator(Id!!, CometChatConstants.RECEIVER_TYPE_USER))
            } else {
                endTyping(TypingIndicator(Id!!, CometChatConstants.RECEIVER_TYPE_GROUP))
            }
        } else {
            if (type == CometChatConstants.RECEIVER_TYPE_USER) {
                startTyping(TypingIndicator(Id!!, CometChatConstants.RECEIVER_TYPE_USER))
            } else {
                startTyping(TypingIndicator(Id!!, CometChatConstants.RECEIVER_TYPE_GROUP))
            }
        }
    }

    private fun endTypingTimer() {
        if (typingTimer != null) {
            typingTimer.schedule(object : TimerTask() {
                override fun run() {
                    sendTypingIndicator(true)
                }
            }, 2000)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(Companion.TAG, "onActivityResult: ")
        when (requestCode) {
            UIKitConstants.RequestCode.AUDIO -> if (data != null) {
                val file = MediaUtils.getRealPath(context, data.data)
                val cr = activity!!.contentResolver
                sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_AUDIO)
            }
            UIKitConstants.RequestCode.GALLERY -> if (data != null) {
                val file = MediaUtils.getRealPath(context, data.data)
                val cr = activity!!.contentResolver
                val mimeType = cr.getType(data.data!!)
                if (mimeType != null && mimeType.contains("image")) {
                    if (file.exists()) sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_IMAGE) else Snackbar.make(rvChatListView!!, R.string.file_not_exist, Snackbar.LENGTH_LONG).show()
                } else {
                    if (file.exists()) sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_VIDEO) else Snackbar.make(rvChatListView!!, R.string.file_not_exist, Snackbar.LENGTH_LONG).show()
                }
            }
            UIKitConstants.RequestCode.CAMERA -> {
                val file: File
                file = if (Build.VERSION.SDK_INT >= 29) {
                    MediaUtils.getRealPath(context, MediaUtils.uri)
                } else {
                    File(MediaUtils.pictureImagePath)
                }
                if (file.exists()) sendMediaMessage(file, CometChatConstants.MESSAGE_TYPE_IMAGE) else Snackbar.make(rvChatListView!!, R.string.file_not_exist, Snackbar.LENGTH_LONG).show()
            }
            UIKitConstants.RequestCode.FILE -> if (data != null) sendMediaMessage(MediaUtils.getRealPath(activity, data.data), CometChatConstants.MESSAGE_TYPE_FILE)
            UIKitConstants.RequestCode.BLOCK_USER -> name = data!!.getStringExtra("")
            UIKitConstants.RequestCode.LOCATION -> {
                locationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(context, "Gps enabled", Toast.LENGTH_SHORT).show()
                    getLocation()
                } else {
                    Toast.makeText(context, "Gps disabled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendMediaMessage(file: File, filetype: String) {
        val progressDialog: ProgressDialog
        progressDialog = ProgressDialog.show(context, "", "Sending Media Message")
        val mediaMessage: MediaMessage
        mediaMessage = if (type.equals(CometChatConstants.RECEIVER_TYPE_USER, ignoreCase = true)) MediaMessage(Id, file, filetype, CometChatConstants.RECEIVER_TYPE_USER) else MediaMessage(Id, file, filetype, CometChatConstants.RECEIVER_TYPE_GROUP)
        val jsonObject = JSONObject()
        try {
            jsonObject.put("path", file.absolutePath)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mediaMessage.metadata = jsonObject
        mediaMessage.parentMessageId = parentId
        sendMediaMessage(mediaMessage, object : CallbackListener<MediaMessage>() {
            override fun onSuccess(mediaMessage: MediaMessage) {
                progressDialog.dismiss()
                noReplyMessages!!.visibility = View.GONE
                Log.d(Companion.TAG, "sendMediaMessage onSuccess: $mediaMessage")
                if (messageAdapter != null) {
                    setReply()
                    messageAdapter!!.addMessage(mediaMessage)
                    scrollToBottom()
                }
            }

            override fun onError(e: CometChatException) {
                progressDialog.dismiss()
                if (activity != null) {
                    ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                }
            }
        })
    }
    private fun getUser() {
        getUser(Id!!, object : CallbackListener<User>() {
            override fun onSuccess(user: User) {
                if (activity != null) {
                    if (user.isBlockedByMe) {
                        isBlockedByMe = true
                        rvSmartReply!!.visibility = View.GONE
                        toolbar!!.isSelected = false
                        blockedUserName!!.text = "You've blocked " + user.name
                        blockUserLayout!!.visibility = View.VISIBLE
                    } else {
                        isBlockedByMe = false
                        blockUserLayout!!.visibility = View.GONE
                    }
                    tvName!!.setText(String.format(getString(R.string.thread_in_name), user.name))
                    Log.d(Companion.TAG, "onSuccess: $user")
                }
            }

            override fun onError(e: CometChatException) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setAvatar() {
        if (avatarUrl != null && !avatarUrl!!.isEmpty()) senderAvatar!!.setAvatar(avatarUrl!!) else {
            senderAvatar!!.setInitials(name!!)
        }
    }
    private fun getGroup() {
        getGroup(Id!!, object : CallbackListener<Group>() {
            override fun onSuccess(group: Group) {
                if (activity != null) {
                    loggedInUserScope = group.scope
                    groupOwnerId = group.owner
                    tvName!!.text = String.format(getString(R.string.thread_in_name), group.name)
                }
            }

            override fun onError(e: CometChatException) {}
        })
    }

    private fun sendMessage(message: String) {
        val textMessage: TextMessage
        textMessage = if (type.equals(CometChatConstants.RECEIVER_TYPE_USER, ignoreCase = true)) TextMessage(Id!!, message, CometChatConstants.RECEIVER_TYPE_USER) else TextMessage(Id!!, message, CometChatConstants.RECEIVER_TYPE_GROUP)
        textMessage.parentMessageId = parentId
        sendTypingIndicator(true)
        sendMessage(textMessage, object : CallbackListener<TextMessage?>() {
            override fun onSuccess(textMessage: TextMessage?) {
                noReplyMessages!!.visibility = View.GONE
                isSmartReplyClicked = false
                if (messageAdapter != null) {
                    setReply()
                    MediaUtils.playSendSound(context, R.raw.outgoing_message)
                    messageAdapter!!.addMessage(textMessage)
                    scrollToBottom()
                }
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                Log.d(Companion.TAG, "onError: " + e.message)
            }
        })
    }

    private fun deleteMessage(baseMessage: BaseMessage) {
        deleteMessage(baseMessage.id, object : CallbackListener<BaseMessage?>() {
            override fun onSuccess(baseMessage: BaseMessage?) {
                if (messageAdapter != null) messageAdapter!!.setUpdatedMessage(baseMessage)
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                Log.d(Companion.TAG, "onError: " + e.message)
            }
        })
    }

    private fun editMessage(baseMessage: BaseMessage?, message: String?) {
        isEdit = false
        isParent = true
        val textMessage: TextMessage
        textMessage = if (baseMessage!!.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER, ignoreCase = true)) TextMessage(baseMessage!!.receiverUid, message!!, CometChatConstants.RECEIVER_TYPE_USER) else TextMessage(baseMessage!!.receiverUid, message!!, CometChatConstants.RECEIVER_TYPE_GROUP)
        sendTypingIndicator(true)
        textMessage.id = baseMessage.id
        editMessage(textMessage, object : CallbackListener<BaseMessage>() {
            override fun onSuccess(message: BaseMessage) {
                if (messageAdapter != null) {
                    Log.e(Companion.TAG, "onSuccess: $message")
                    messageAdapter!!.setUpdatedMessage(message)
                }
            }

            override fun onError(e: CometChatException) {
                ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                Log.d(Companion.TAG, "onError: " + e.message)
            }
        })
    }

    private fun replyMessage(baseMessage: BaseMessage?, message: String) {
        isReply = false
        try {
            val textMessage: TextMessage
            textMessage = if (type.equals(CometChatConstants.RECEIVER_TYPE_USER, ignoreCase = true)) TextMessage(Id!!, message, CometChatConstants.RECEIVER_TYPE_USER) else TextMessage(Id!!, message, CometChatConstants.RECEIVER_TYPE_GROUP)
            val jsonObject = JSONObject()
            val replyObject = JSONObject()
            if (baseMessage!!.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
                replyObject.put("type", CometChatConstants.MESSAGE_TYPE_TEXT)
                replyObject.put("message", (baseMessage as TextMessage).text)
            } else if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_IMAGE) {
                replyObject.put("type", CometChatConstants.MESSAGE_TYPE_IMAGE)
                replyObject.put("message", "image")
            } else if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_VIDEO) {
                replyObject.put("type", CometChatConstants.MESSAGE_TYPE_VIDEO)
                replyObject.put("message", "video")
            } else if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_FILE) {
                replyObject.put("type", CometChatConstants.MESSAGE_TYPE_FILE)
                replyObject.put("message", "file")
            } else if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_AUDIO) {
                replyObject.put("type", CometChatConstants.MESSAGE_TYPE_AUDIO)
                replyObject.put("message", "audio")
            }
            replyObject.put("name", baseMessage.sender.name)
            replyObject.put("avatar", baseMessage.sender.avatar)
            jsonObject.put("reply", replyObject)
            textMessage.parentMessageId = parentId
            textMessage.metadata = jsonObject
            sendTypingIndicator(true)
            sendMessage(textMessage, object : CallbackListener<TextMessage?>() {
                override fun onSuccess(textMessage: TextMessage?) {
                    if (messageAdapter != null) {
                        MediaUtils.playSendSound(context, R.raw.outgoing_message)
                        messageAdapter!!.addMessage(textMessage)
                        scrollToBottom()
                    }
                }

                override fun onError(e: CometChatException) {
                    ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                    Log.e(Companion.TAG, "onError: " + e.message)
                }
            })
        } catch (e: Exception) {
            Log.e(Companion.TAG, "replyMessage: " + e.message)
        }
    }

    private fun scrollToBottom() {
        if (messageAdapter != null && messageAdapter!!.getItemCount() > 0) {
            rvChatListView!!.scrollToPosition(messageAdapter!!.getItemCount() - 1)
            val scrollViewHeight = nestedScrollView!!.height
            if (scrollViewHeight > 0) {
                val lastView = nestedScrollView!!.getChildAt(nestedScrollView!!.childCount - 1)
                val lastViewBottom = lastView.bottom + nestedScrollView!!.paddingBottom
                val deltaScrollY = lastViewBottom - scrollViewHeight - nestedScrollView!!.scrollY
                /* If you want to see the scroll animation, call this. */nestedScrollView!!.smoothScrollBy(0, deltaScrollY)
            }
        }
    }


    private fun addMessageListener() {
        addMessageListener(Companion.TAG, object : MessageListener() {
            override fun onTextMessageReceived(message: TextMessage) {
                Log.d(Companion.TAG, "onTextMessageReceived: $message")
                onMessageReceived(message)
            }

            override fun onMediaMessageReceived(message: MediaMessage) {
                Log.d(Companion.TAG, "onMediaMessageReceived: $message")
                onMessageReceived(message)
            }

            override fun onCustomMessageReceived(message: CustomMessage) {
                Log.d(Companion.TAG, "onCustomMessageReceived: $message")
                onMessageReceived(message)
            }

            override fun onTypingStarted(typingIndicator: TypingIndicator) {
                Log.e(Companion.TAG, "onTypingStarted: $typingIndicator")
                setTypingIndicator(typingIndicator, true)
            }

            override fun onTypingEnded(typingIndicator: TypingIndicator) {
                Log.d(Companion.TAG, "onTypingEnded: $typingIndicator")
                setTypingIndicator(typingIndicator, false)
            }

            override fun onTransientMessageReceived(transientMessage: TransientMessage?) {
                setTransientMessage(transientMessage)
            }

            override fun onMessagesDelivered(messageReceipt: MessageReceipt) {
                Log.d(Companion.TAG, "onMessagesDelivered: $messageReceipt")
                setMessageReciept(messageReceipt)
            }

            override fun onMessagesRead(messageReceipt: MessageReceipt) {
                Log.e(Companion.TAG, "onMessagesRead: $messageReceipt")
                setMessageReciept(messageReceipt)
            }

            override fun onMessageEdited(message: BaseMessage) {
                Log.d(Companion.TAG, "onMessageEdited: $message")
                if (parentId != message.getId())
                    updateMessage(message)
                else {
                    reactionInfo = Extensions.getReactionsOnMessage(message)
                    reactionLayout.removeAllViews()
                    setReactionForParentMessage()
                }
            }

            override fun onMessageDeleted(message: BaseMessage) {
                Log.d(Companion.TAG, "onMessageDeleted: ")
                updateMessage(message)
            }
        })
    }

    private fun setTransientMessage(transientMessage: TransientMessage?) {
        if (transientMessage?.data != null) {
            try {
                val reaction = transientMessage.data.getString("reaction")
                val type = transientMessage.data.getString("type")
                if (reaction.equals("heart") && type.equals("live_reaction")) setReaction()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setMessageReciept(messageReceipt: MessageReceipt) {
        if (messageAdapter != null) {
            if (messageReceipt.receivertype == CometChatConstants.RECEIVER_TYPE_USER) {
                if (Id != null && messageReceipt.sender.uid == Id) {
                    if (messageReceipt.receiptType == MessageReceipt.RECEIPT_TYPE_DELIVERED) messageAdapter!!.setDeliveryReceipts(messageReceipt) else messageAdapter!!.setReadReceipts(messageReceipt)
                }
            }
        }
    }

    private fun setTypingIndicator(typingIndicator: TypingIndicator, isShow: Boolean) {
        if (typingIndicator.receiverType.equals(CometChatConstants.RECEIVER_TYPE_USER, ignoreCase = true)) {
            Log.e(Companion.TAG, "onTypingStarted: $typingIndicator")
            if (Id != null && Id.equals(typingIndicator.sender.uid, ignoreCase = true)) {
                if (typingIndicator.metadata == null) typingIndicator(typingIndicator, isShow)
            }
        } else {
            if (Id != null && Id.equals(typingIndicator.receiverId, ignoreCase = true)) typingIndicator(typingIndicator, isShow)
        }
    }

    private fun onMessageReceived(message: BaseMessage) {
        MediaUtils.playSendSound(context, R.raw.incoming_message)
        if (message.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
            if (Id != null && Id.equals(message.sender.uid, ignoreCase = true)) {
                if (message.parentMessageId == parentId) setMessage(message)
            } else if (Id != null && Id.equals(message.receiverUid, ignoreCase = true) && message.sender.uid.equals(loggedInUser.uid, ignoreCase = true)) {
                if (message.parentMessageId == parentId) setMessage(message)
            }
        } else {
            if (Id != null && Id.equals(message.receiverUid, ignoreCase = true)) {
                if (message.parentMessageId == parentId) setMessage(message)
            }
        }
    }

    private fun updateMessage(message: BaseMessage) {
        messageAdapter!!.setUpdatedMessage(message)
    }

    private fun setMessage(message: BaseMessage) {
        setReply()
        noReplyMessages!!.visibility = View.GONE
        if (messageAdapter != null) {
            messageAdapter!!.addMessage(message)
            checkSmartReply(message)
            markAsRead(message)
            if (messageAdapter!!.itemCount - 1 - (rvChatListView!!.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() < 5) scrollToBottom()
        } else {
            messageList.add(message)
            initMessageAdapter(messageList)
        }
    }

    private fun setReply() {
        replyCount += 1
        if (replyCount == 1) tvReplyCount!!.text = "$replyCount Reply" else tvReplyCount!!.text = "$replyCount Replies"
    }

    private fun checkSmartReply(lastMessage: BaseMessage?) {
        if (lastMessage != null && lastMessage.sender.uid != loggedInUser.uid) {
            if (lastMessage.metadata != null) {
                getSmartReplyList(lastMessage)
            }
        }
    }

    private fun typingIndicator(typingIndicator: TypingIndicator, show: Boolean) {
        if (messageAdapter != null) {
            if (show) {
                if (typingIndicator.receiverType == CometChatConstants.RECEIVER_TYPE_USER) {
                    if (typingIndicator.metadata == null) {
                        FeatureRestriction.isTypingIndicatorsEnabled(object : FeatureRestriction.OnSuccessListener{
                            override fun onSuccess(p0: Boolean) {
                                if (p0) {
                                    tvTypingIndicator?.visibility = View.VISIBLE
                                    tvTypingIndicator?.text = "is Typing..."
                                }
                            }
                        })
                    }
                }
                else {
                    if (typingIndicator.metadata == null) {
                        FeatureRestriction.isTypingIndicatorsEnabled(object : FeatureRestriction.OnSuccessListener{
                            override fun onSuccess(p0: Boolean) {
                                if (p0) {
                                    tvTypingIndicator?.visibility = View.VISIBLE
                                    tvTypingIndicator!!.text = typingIndicator.sender.name + " is Typing..."
                                }
                            }
                        })
                    }
                }
            } else {
                tvTypingIndicator!!.visibility = View.GONE
            }
        }
    }

    private fun removeMessageListener() {
        removeMessageListener(Companion.TAG)
    }

    private fun removeUserListener() {
        removeUserListener(Companion.TAG)
    }

    override fun onPause() {
        Log.d(Companion.TAG, "onPause: ")
        super.onPause()
        if (messageAdapter != null) messageAdapter!!.stopPlayingAudio()
        removeMessageListener()
        sendTypingIndicator(true)
    }

    override fun onResume() {
        super.onResume()
        Log.d(Companion.TAG, "onResume: ")
        messageAdapter = null
        messagesRequest = null
        checkOnGoingCall()
        fetchMessage()
        isNoMoreMessages = false
        addMessageListener()
        if (type != null) {
            if (type == CometChatConstants.RECEIVER_TYPE_USER) {
                Thread(Runnable { this.getUser() }).start()
            } else {
                Thread(Runnable { this.getGroup() }).start()
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.contxt = context
    }

    override fun onDetach() {
        super.onDetach()
    }


    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.iv_close_message_action) {
            if (messageAdapter != null) {
                messageAdapter!!.clearLongClickSelectedItem()
                messageAdapter!!.notifyDataSetChanged()
            }
        } else if (id == R.id.ic_more_option) {
            val messageActionFragment = CometChatMessageActions()
            val bundle = Bundle()
            if (messageType == CometChatConstants.MESSAGE_TYPE_TEXT) bundle.putBoolean("copyVisible", true) else bundle.putBoolean("copyVisible", false)
            bundle.putBoolean("forwardVisible", true)
            if (name == loggedInUser.name && messageType == CometChatConstants.MESSAGE_TYPE_TEXT) {
                bundle.putBoolean("editVisible", true)
            } else {
                bundle.putBoolean("editVisible", false)
            }
            bundle.putString("type", CometChatThreadMessageListActivity::class.java.name)
            messageActionFragment.arguments = bundle
            showBottomSheet(messageActionFragment)
        } else if (id == R.id.ic_forward_option) {
            isParent = true
            startForwardThreadActivity()
        } else if (id == R.id.iv_message_close) {
            if (messageAdapter != null) {
                messageAdapter!!.clearLongClickSelectedItem()
                messageAdapter!!.notifyDataSetChanged()
            }
            isEdit = false
            baseMessage = null
            editMessageLayout!!.visibility = View.GONE
        } else if (id == R.id.iv_reply_close) {
            if (messageAdapter != null) {
                messageAdapter!!.clearLongClickSelectedItem()
                messageAdapter!!.notifyDataSetChanged()
            }
            isReply = false
            baseMessage = null
            replyMessageLayout!!.visibility = View.GONE
        } else if (id == R.id.btn_unblock_user) {
            unblockUser()
        }
//        else if (id == R.id.chatList_toolbar) {
//            if (type == CometChatConstants.RECEIVER_TYPE_USER) {
//                val intent = Intent(context, CometChatUserDetailScreenActivity::class.java)
//                intent.putExtra(StringContract.IntentStrings.UID, Id)
//                intent.putExtra(StringContract.IntentStrings.NAME, name)
//                intent.putExtra(StringContract.IntentStrings.AVATAR, avatarUrl)
//                intent.putExtra(StringContract.IntentStrings.IS_BLOCKED_BY_ME, isBlockedByMe)
//                intent.putExtra(StringContract.IntentStrings.TYPE, type)
//                startActivity(intent)
//            } else {
//                val intent = Intent(context, CometChatGroupDetailScreenActivity::class.java)
//                intent.putExtra(StringContract.IntentStrings.GUID, Id)
//                intent.putExtra(StringContract.IntentStrings.NAME, name)
//                intent.putExtra(StringContract.IntentStrings.AVATAR, avatarUrl)
//                intent.putExtra(StringContract.IntentStrings.TYPE, type)
//                intent.putExtra(StringContract.IntentStrings.MEMBER_SCOPE, loggedInUserScope)
//                intent.putExtra(StringContract.IntentStrings.GROUP_OWNER, groupOwnerId)
//                startActivity(intent)
//            }
//        }
    }

    override fun setLongMessageClick(baseMessagesList: List<BaseMessage>?) {
        Log.e(Companion.TAG, "setLongMessageClick: $baseMessagesList")
        isReply = false
        isEdit = false
        isParent = false
        val messageActionFragment = CometChatMessageActions()
        replyMessageLayout!!.visibility = View.GONE
        editMessageLayout!!.visibility = View.GONE

        FeatureRestriction.isReactionsEnabled(object : FeatureRestriction.OnSuccessListener {
            override fun onSuccess(p0: Boolean) {
                isReactionsVisible = p0
                Log.e("TAG", "onSuccess: inside " +p0 )
            }
        })
        FeatureRestriction.isDeleteMemberMessageEnabled(object : FeatureRestriction.OnSuccessListener {
            override fun onSuccess(p0: Boolean) {
                isDeleteMemberMessageVisible = p0
                Log.e("TAG", "onSuccess: inside " +p0 )
            }
        })
        var copyVisible = FeatureRestriction.isShareCopyForwardMessageEnabled()
        var threadVisible = false
        var replyVisible = false
        var editVisible = FeatureRestriction.isEditMessageEnabled()
        var deleteVisible = FeatureRestriction.isDeleteMessageEnabled()
        var forwardVisible = FeatureRestriction.isShareCopyForwardMessageEnabled()
        var mapVisible = true
        var reactionVisible = isReactionsVisible
        var sendMessagePrivatelyVisible = false
        var messageInfoVisible = false

        var metadata = JSONObject()
        val textMessageList: MutableList<BaseMessage> = ArrayList()
        val mediaMessageList: MutableList<BaseMessage> = ArrayList()
        val locationMessageList: MutableList<BaseMessage> = ArrayList()
        for (baseMessage in baseMessagesList!!) {
            if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
                textMessageList.add(baseMessage)
            } else if (baseMessage.type == CometChatConstants.MESSAGE_TYPE_IMAGE || baseMessage.type == CometChatConstants.MESSAGE_TYPE_VIDEO || baseMessage.type == CometChatConstants.MESSAGE_TYPE_FILE || baseMessage.type == CometChatConstants.MESSAGE_TYPE_AUDIO) {
                mediaMessageList.add(baseMessage)
            } else {
                locationMessageList.add(baseMessage)
            }
        }
        if (textMessageList.size == 1) {
            val basemessage = textMessageList[0]
            if (basemessage != null && basemessage.sender != null) {
                if (basemessage !is Action && basemessage.deletedAt == 0L) {
                    baseMessage = basemessage
                    threadVisible = false
                    mapVisible = false
                    if (basemessage.sender.uid == getLoggedInUser().uid) {
                        deleteVisible = FeatureRestriction.isDeleteMessageEnabled()
                        editVisible = FeatureRestriction.isEditMessageEnabled()
                        forwardVisible = true
                    } else {
                        editVisible = false
                        forwardVisible = true
                        deleteVisible = if (loggedInUserScope != null && (loggedInUserScope == CometChatConstants.SCOPE_ADMIN || loggedInUserScope == CometChatConstants.SCOPE_MODERATOR)) (FeatureRestriction.isDeleteMessageEnabled() || isDeleteMemberMessageVisible) else false
                    }
                }
            }
        }
        if (mediaMessageList.size == 1) {
            val basemessage = mediaMessageList[0]
            if (basemessage != null && basemessage.sender != null) {
                if (basemessage !is Action && basemessage.deletedAt == 0L) {
                    baseMessage = basemessage
                    copyVisible = false
                    threadVisible = false
                    mapVisible = false
                    if (basemessage.sender.uid == getLoggedInUser().uid) {
                        deleteVisible = FeatureRestriction.isDeleteMessageEnabled()
                        editVisible = false
//                        forwardVisible = true
                    } else {
                        deleteVisible = if (loggedInUserScope != null && (loggedInUserScope == CometChatConstants.SCOPE_ADMIN || loggedInUserScope == CometChatConstants.SCOPE_MODERATOR))  (FeatureRestriction.isDeleteMessageEnabled() || isDeleteMemberMessageVisible) else false
//                        forwardVisible = true
                        editVisible = false
                    }
                }
            }
        }
        if (locationMessageList.size == 1) {
            val basemessage = locationMessageList[0]
            if (basemessage != null && basemessage.sender != null) {
                if (basemessage !is Action && basemessage.deletedAt == 0L) {
                    baseMessage = basemessage
                    threadVisible = false
                    copyVisible = false
                    replyVisible = false
                    forwardVisible = true
                    if (basemessage.sender.uid == getLoggedInUser().uid) {
                        mapVisible = true
                        deleteVisible = FeatureRestriction.isDeleteMessageEnabled()
                        editVisible = false
                    } else {
                        deleteVisible = if (loggedInUserScope != null && (loggedInUserScope == CometChatConstants.SCOPE_ADMIN || loggedInUserScope == CometChatConstants.SCOPE_MODERATOR)) (FeatureRestriction.isDeleteMessageEnabled() || isDeleteMemberMessageVisible) else false
                        mapVisible = true
                        editVisible = false
                    }
                }
            }
        }
        baseMessages = baseMessagesList
        val bundle = Bundle()
        bundle.putBoolean("copyVisible", copyVisible)
        bundle.putBoolean("threadVisible", threadVisible)
        bundle.putBoolean("editVisible", editVisible)
        bundle.putBoolean("deleteVisible", deleteVisible)
        bundle.putBoolean("replyVisible", replyVisible)
        bundle.putBoolean("forwardVisible", forwardVisible)
        bundle.putBoolean("mapVisible", mapVisible)
        bundle.putBoolean("reactionVisible", reactionVisible)
//        if (isExtensionEnabled("reactions"))
//            bundle.putBoolean("reactionVisible", reactionVisible)

//        isExtensionEnabled("reactions", object : CallbackListener<Boolean>() {
//            override fun onSuccess(p0: Boolean?) {
//                if (p0 as Boolean) bundle.putBoolean("reactionVisible", reactionVisible)
//            }
//
//            override fun onError(p0: CometChatException?) {
//                Toast.makeText(context, "Error:" + p0?.message, Toast.LENGTH_SHORT).show()
//            }
//
//        })

        if (baseMessage?.receiverType == CometChatConstants.RECEIVER_TYPE_GROUP) {
            if (baseMessage?.sender?.uid == loggedInUser.uid) {
                messageInfoVisible = true
                bundle.putBoolean("messageInfoVisible", messageInfoVisible)
            }
            if (baseMessage?.sender?.uid != loggedInUser.uid) {
                sendMessagePrivatelyVisible = true
                metadata.put("messageSenderName", baseMessage?.sender?.name)
                metadata.put("messageSenderUid", baseMessage?.sender?.uid)
                metadata.put("messageSenderAvatar", baseMessage?.sender?.avatar)
                metadata.put("messageSenderStatus", baseMessage?.sender?.status)
                bundle.putBoolean("sendMessagePrivately", sendMessagePrivatelyVisible)
                bundle.putString("metadata", metadata.toString())
            }
        }

        bundle.putString("type", CometChatThreadMessageListActivity::class.java.name)
        messageActionFragment.arguments = bundle
        showBottomSheet(messageActionFragment)
    }

    private fun showBottomSheet(cometChatMessageActions: CometChatMessageActions) {
        cometChatMessageActions.show(fragmentManager!!, cometChatMessageActions.tag)
        cometChatMessageActions.setMessageActionListener(object : MessageActionListener {
            override fun onThreadMessageClick() {}
            override fun onEditMessageClick() {
                if (isParent) editParentMessage() else editThreadMessage()
            }

            override fun onReplyMessageClick() {}
            override fun onForwardMessageClick() {
                if (isParent) startForwardThreadActivity() else startForwardMessageActivity()
            }

            override fun onDeleteMessageClick() {
                deleteMessage(baseMessage!!)
                if (messageAdapter != null) {
                    messageAdapter!!.clearLongClickSelectedItem()
                    messageAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCopyMessageClick() {
                var copyMessage = ""
                if (isParent) {
                    copyMessage = message!!
                }
                for (bMessage in baseMessages) {
                    if (bMessage.deletedAt == 0L && bMessage is TextMessage) {
                        copyMessage = copyMessage + "[" + Utils.getLastMessageDate(bMessage.getSentAt()) + "] " + bMessage.getSender().name + ": " + (bMessage as TextMessage).text
                    }
                }
                Log.e(Companion.TAG, "onCopy: $message")
                val clipboardManager = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("ThreadMessageAdapter", copyMessage)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(context, resources.getString(R.string.text_copied), Toast.LENGTH_LONG).show()
                isParent = true
                if (messageAdapter != null) {
                    messageAdapter!!.clearLongClickSelectedItem()
                    messageAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onShareMessageClick() {
                shareMessage()
            }

            override fun onMessageInfoClick() {
                val intent = Intent(context, CometChatMessageInfoScreenActivity::class.java)
                if (isParent) {
                } else {
                    intent.putExtra(UIKitConstants.IntentStrings.ID, baseMessage!!.id)
                    intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE, baseMessage!!.type)
                    intent.putExtra(UIKitConstants.IntentStrings.SENTAT, baseMessage!!.sentAt)
                    if (baseMessage!!.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
                        intent.putExtra(UIKitConstants.IntentStrings.TEXTMESSAGE,
                                Extensions.getProfanityFilter(baseMessage!!))
                    } else if (baseMessage!!.category == CometChatConstants.CATEGORY_CUSTOM) {
                        intent.putExtra(UIKitConstants.IntentStrings.CUSTOM_MESSAGE,
                                (baseMessage as CustomMessage).customData.toString())
                        intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE,
                                CometChatConstants.CATEGORY_CUSTOM)
                    } else {
                        FeatureRestriction.isImageModerationEnabled(object : FeatureRestriction.OnSuccessListener{
                            override fun onSuccess(p0: Boolean) {
                                isImageNotSafe = Extensions.getImageModeration(context, baseMessage)
                            }
                        })
                        intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL,
                                (baseMessage as MediaMessage).attachment.fileUrl)
                        intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME,
                                (baseMessage as MediaMessage).attachment.fileName)
                        intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE,
                                (baseMessage as MediaMessage).attachment.fileSize)
                        intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION,
                                (baseMessage as MediaMessage).attachment.fileExtension)
                        intent.putExtra(UIKitConstants.IntentStrings.IMAGE_MODERATION, isImageNotSafe)
                    }
                }
                context!!.startActivity(intent)
            }

            override fun onReactionClick(reaction: Reaction) {
                if (reaction.name == "add_reaction") {
                    val reactionDialog = CometChatReactionDialog()
                    reactionDialog.setOnEmojiClick(object : OnReactionClickListener {
                        override fun onEmojiClicked(emojicon: Reaction) {
                            sendReaction(emojicon)
                            reactionDialog.dismiss()
                        }
                    })
                    reactionDialog.show(fragmentManager!!, "ReactionDialog")
                } else {
                    sendReaction(reaction)
                }
            }

            override fun onSendMessagePrivatelyClick() {
                val intent = Intent(context, CometChatMessageListActivity::class.java)
                intent.putExtra(UIKitConstants.IntentStrings.UID, baseMessage?.sender?.uid)
                intent.putExtra(UIKitConstants.IntentStrings.AVATAR, baseMessage?.sender?.avatar)
                intent.putExtra(UIKitConstants.IntentStrings.STATUS, baseMessage?.sender?.status)
                intent.putExtra(UIKitConstants.IntentStrings.NAME, baseMessage?.sender?.name)
                intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_USER)
                startActivity(intent)
                activity?.finish()
            }

            override fun onReplyPrivatelyClick() {
                val intent = Intent(context, CometChatMessageListActivity::class.java)
                intent.putExtra(UIKitConstants.IntentStrings.UID, baseMessage?.sender?.uid)
                intent.putExtra(UIKitConstants.IntentStrings.AVATAR, baseMessage?.sender?.avatar)
                intent.putExtra(UIKitConstants.IntentStrings.STATUS,  baseMessage?.sender?.status)
                intent.putExtra(UIKitConstants.IntentStrings.NAME, baseMessage?.sender?.name)
                intent.putExtra("isReply", true)
                intent.putExtra("baseMessageMetadata", baseMessage?.rawMessage.toString())

                intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_USER)
                startActivity(intent)
                activity?.finish()
            }
        })
    }

    private fun sendReaction(reaction: Reaction) {
        val body = JSONObject()
        try {
            body.put("msgId", baseMessage!!.id)
            body.put("emoji", reaction.name)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        callExtension("reactions", "POST", "/v1/react", body,
                object : CallbackListener<JSONObject>() {
                    override fun onSuccess(responseObject: JSONObject) {
                        Log.e(Companion.TAG, "onSuccess: $responseObject")
                        // ReactionModel added successfully.
                    }

                    override fun onError(e: CometChatException) {
                        ErrorMessagesUtils.cometChatErrorMessage(context, e.code)
                        Log.e(Companion.TAG, "onError: " + e.code + e.message + e.details)
                    }
                })
    }

    private fun editParentMessage() {
        if (message != null && messageType == CometChatConstants.MESSAGE_TYPE_TEXT) {
            isEdit = true
            isReply = false
            tvMessageTitle!!.text = resources.getString(R.string.edit_message)
            tvMessageSubTitle!!.text = message
            composeBox!!.ivSend!!.visibility = View.VISIBLE
            editMessageLayout!!.visibility = View.VISIBLE
            composeBox!!.etComposeBox!!.setText(message)
        }
    }

    private fun shareMessage() {
        if (baseMessage != null && baseMessage!!.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_TITLE, resources.getString(R.string.app_name))
            shareIntent.putExtra(Intent.EXTRA_TEXT, (baseMessage as TextMessage).text)
            shareIntent.type = "text/plain"
            val intent = Intent.createChooser(shareIntent, resources.getString(R.string.share_message))
            startActivity(intent)
        } else if (baseMessage != null && baseMessage!!.type == CometChatConstants.MESSAGE_TYPE_IMAGE) {
            val mediaName = (baseMessage as MediaMessage).attachment.fileName
            Glide.with(context!!).asBitmap().load((baseMessage as MediaMessage).attachment.fileUrl).into(object : SimpleTarget<Bitmap?>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    val path = MediaStore.Images.Media.insertImage(context!!.contentResolver, resource, mediaName, null)
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
                    shareIntent.type = (baseMessage as MediaMessage).attachment.fileMimeType
                    val intent = Intent.createChooser(shareIntent, resources.getString(R.string.share_message))
                    startActivity(intent)
                }
            })
        }
    }

    private fun editThreadMessage() {
        if (baseMessage != null && baseMessage!!.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
            isEdit = true
            isReply = false
            tvMessageTitle!!.text = resources.getString(R.string.edit_message)
            tvMessageSubTitle!!.text = (baseMessage as TextMessage).text
            composeBox!!.ivSend!!.visibility = View.VISIBLE
            editMessageLayout!!.visibility = View.VISIBLE
            composeBox!!.etComposeBox!!.setText((baseMessage as TextMessage).text)
            if (messageAdapter != null) {
                messageAdapter!!.setSelectedMessage(baseMessage!!.getId())
                messageAdapter!!.notifyDataSetChanged()
            }
        }
    }

    private fun startForwardThreadActivity() {
        val intent = Intent(context, CometChatForwardMessageActivity::class.java)
        if (parentMessageCategory == CometChatConstants.CATEGORY_MESSAGE) {
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY, CometChatConstants.CATEGORY_MESSAGE)
            intent.putExtra(UIKitConstants.IntentStrings.TYPE, messageType)
            if (messageType == CometChatConstants.MESSAGE_TYPE_TEXT) {
                intent.putExtra(CometChatConstants.MESSAGE_TYPE_TEXT, message)
                intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.MESSAGE_TYPE_TEXT)
            } else if (messageType == CometChatConstants.MESSAGE_TYPE_IMAGE || messageType == CometChatConstants.MESSAGE_TYPE_AUDIO || messageType == CometChatConstants.MESSAGE_TYPE_VIDEO || messageType == CometChatConstants.MESSAGE_TYPE_FILE) {
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, message)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL, message)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE, messageMimeType)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, messageExtension)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, messageSize)
            }
        } else {
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY, CometChatConstants.CATEGORY_CUSTOM)
            intent.putExtra(UIKitConstants.IntentStrings.TYPE, UIKitConstants.IntentStrings.LOCATION)
            try {
                intent.putExtra(UIKitConstants.IntentStrings.LOCATION_LATITUDE, parentMessageLatitude)
                intent.putExtra(UIKitConstants.IntentStrings.LOCATION_LONGITUDE, parentMessageLongitude)
            } catch (e: Exception) {
                Log.e(Companion.TAG, "startForwardMessageActivityError: " + e.message)
            }
        }
        startActivity(intent)
    }

    private fun startForwardMessageActivity() {
        val intent = Intent(context, CometChatForwardMessageActivity::class.java)
        if (baseMessage!!.category == CometChatConstants.CATEGORY_MESSAGE) {
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY, CometChatConstants.CATEGORY_MESSAGE)
            if (baseMessage!!.type == CometChatConstants.MESSAGE_TYPE_TEXT) {
                intent.putExtra(CometChatConstants.MESSAGE_TYPE_TEXT, (baseMessage as TextMessage).text)
                intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.MESSAGE_TYPE_TEXT)
            } else if (baseMessage!!.type == CometChatConstants.MESSAGE_TYPE_IMAGE || baseMessage!!.type == CometChatConstants.MESSAGE_TYPE_AUDIO || baseMessage!!.type == CometChatConstants.MESSAGE_TYPE_VIDEO || baseMessage!!.type == CometChatConstants.MESSAGE_TYPE_FILE) {
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_NAME, (baseMessage as MediaMessage).attachment.fileName)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_URL, (baseMessage as MediaMessage).attachment.fileUrl)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_MIME_TYPE, (baseMessage as MediaMessage).attachment.fileMimeType)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_EXTENSION, (baseMessage as MediaMessage).attachment.fileExtension)
                intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_TYPE_IMAGE_SIZE, (baseMessage as MediaMessage).attachment.fileSize)
                intent.putExtra(UIKitConstants.IntentStrings.TYPE, baseMessage!!.type)
            }
        } else {
            intent.putExtra(UIKitConstants.IntentStrings.MESSAGE_CATEGORY, CometChatConstants.CATEGORY_CUSTOM)
            intent.putExtra(UIKitConstants.IntentStrings.TYPE, UIKitConstants.IntentStrings.LOCATION)
            try {
                intent.putExtra(UIKitConstants.IntentStrings.LOCATION_LATITUDE,
                        (baseMessage as CustomMessage).customData.getDouble("latitude"))
                intent.putExtra(UIKitConstants.IntentStrings.LOCATION_LONGITUDE,
                        (baseMessage as CustomMessage).customData.getDouble("longitude"))
            } catch (e: Exception) {
                Log.e(Companion.TAG, "startForwardMessageActivityError: " + e.message)
            }
        }
        startActivity(intent)
    }

    companion object{
        private const val TAG = "CometChatMessageScreen"
        private const val LIMIT = 30
    }

    override fun handleDialogClose(dialog: DialogInterface?) {
        if (messageAdapter != null) messageAdapter!!.clearLongClickSelectedItem()
        dialog!!.dismiss()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (activity != null) {
                activity!!.onBackPressed()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
