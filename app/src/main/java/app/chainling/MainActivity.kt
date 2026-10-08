package app.chainling

import android.os.Bundle
import android.graphics.*
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.drawable.GradientDrawable
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.solana.mobilewalletadapter.clientlib.*
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity: ComponentActivity() {
    private lateinit var store: ColonyStore
    private lateinit var sender: ActivityResultSender
    private lateinit var adapter: MobileWalletAdapter
    private lateinit var root: LinearLayout
    private var current: Profile? = null
    private var busy = false
    private var primaryScreen = true
    private var activeTab = "creature"
    private val ink=0xFFF3F1FA.toInt(); private val muted=0xFF9DA6BC.toInt(); private val lime=0xFFD6FF91.toInt()
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store=ColonyStore(this); sender=ActivityResultSender(this)
        adapter=MobileWalletAdapter(connectionIdentity=ConnectionIdentity(identityUri=Uri.parse("https://walletkin.invalid"),iconUri=Uri.parse("favicon.ico"),identityName="Walletkin • local MVP"))
        // Analyzer stays read-only; explicit SKR checkout requires wallet signing and server verification.
        adapter.blockchain = Solana.Mainnet
        adapter.authToken=getSharedPreferences("session",MODE_PRIVATE).getString("auth",null)
        val restored=savedInstanceState?.getString("address") ?: getSharedPreferences("session",MODE_PRIVATE).getString("last",null)
        current=restored?.let { store.load(it) }
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(busy) { Toast.makeText(this@MainActivity,"Please wait for the current step to finish",Toast.LENGTH_SHORT).show(); return }
                if(primaryScreen) { finish() } else if(current == null) welcome() else profile()
            }
        })
        if(current == null) welcome() else profile()
    }
    override fun onSaveInstanceState(out: Bundle) { out.putString("address",current?.snapshot?.address); super.onSaveInstanceState(out) }
    private fun screen(eyebrow:String,title:String,subtitle:String) {
        primaryScreen=false
        val frame=FrameLayout(this)
        val cosmetic=current?.let{CosmeticEquipment.item(this,it.snapshot.address,"Backgrounds")}
        frame.addView(if(cosmetic!=null)CosmeticScene(this,cosmetic) else WalletkinBackdrop(this,currentBackground().artKey),FrameLayout.LayoutParams(-1,-1))
        val shell=LinearLayout(this);shell.orientation=LinearLayout.VERTICAL
        frame.addView(shell,FrameLayout.LayoutParams(-1,-1))
        val scroll=ScrollView(this);scroll.isFillViewport=true;scroll.isVerticalScrollBarEnabled=false
        shell.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        root=LinearLayout(this);root.orientation=LinearLayout.VERTICAL;root.setPadding(dp(22),dp(16),dp(22),dp(24));scroll.addView(root)
        setContentView(frame)
        ViewCompat.setOnApplyWindowInsetsListener(frame) { _,insets ->
            val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());shell.setPadding(bars.left,bars.top,bars.right,bars.bottom);insets
        }
        androidx.core.view.WindowInsetsControllerCompat(window,frame).apply { isAppearanceLightStatusBars=false;isAppearanceLightNavigationBars=false }
        val header=LinearLayout(this);header.gravity=Gravity.CENTER_VERTICAL;root.addView(header)
        val brand=text("WALLETKIN",14,lime,header);brand.letterSpacing=0.18f;brand.typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL);brand.layoutParams=LinearLayout.LayoutParams(0,-2,1f)
        val menu=TextView(this);menu.text="•••";menu.textSize=18f;menu.gravity=Gravity.CENTER;menu.setTextColor(ink);menu.contentDescription="Wallet and connection options"
        menu.background=GradientDrawable().apply{setColor(0xB51D263D.toInt());cornerRadius=dp(14).toFloat();setStroke(dp(1),0xFF39445C.toInt())}
        header.addView(menu,LinearLayout.LayoutParams(dp(44),dp(44)));menu.setOnClickListener{if(!busy)walletMenu()}
        space(22);val eyebrowView=text(eyebrow.uppercase(),10,lime);eyebrowView.letterSpacing=0.15f
        space(4);text(title,34,ink);space(6);text(subtitle,14,muted);space(18)
        val nav=LinearLayout(this);nav.gravity=Gravity.CENTER;nav.setPadding(dp(5),dp(5),dp(5),dp(5))
        nav.background=GradientDrawable().apply{setColor(0xEE111929.toInt());cornerRadius=dp(24).toFloat();setStroke(dp(1),0xFF30394C.toInt())}
        shell.addView(nav,LinearLayout.LayoutParams(-1,dp(68)).apply{setMargins(dp(16),dp(4),dp(16),dp(10))})
        listOf(Triple("creature","✦","Creature"),Triple("colony","❖","Colony"),Triple("legends","✧","Legends"),Triple("store","◇","Store")).forEach { (id,icon,label) ->
            val item=TextView(this);item.text="$icon\n$label";item.gravity=Gravity.CENTER;item.textSize=12f;item.setTextColor(if(activeTab==id)lime else muted);item.contentDescription=label
            if(activeTab==id)item.background=GradientDrawable().apply{setColor(0xFF27332F.toInt());cornerRadius=dp(18).toFloat()}
            nav.addView(item,LinearLayout.LayoutParams(0,-1,1f));item.setOnClickListener{if(!busy)when(id){"creature"->if(current==null)welcome() else profile();"colony"->colony();"store"->cosmeticStore();else->legends()}}
        }
    }
    private fun text(value:String,size:Int=15,color:Int=ink,parent:LinearLayout=root):TextView {
        val v=TextView(this);v.text=value;v.textSize=size.toFloat();v.setTextColor(color);v.setLineSpacing(dp(3).toFloat(),1f)
        v.typeface=Typeface.create(if(size>=21)"sans-serif-medium" else "sans-serif",Typeface.NORMAL)
        parent.addView(v);return v
    }
    private fun space(h:Int){root.addView(View(this),LinearLayout.LayoutParams(1,dp(h)))}
    private fun card(parent:LinearLayout=root):LinearLayout {
        val panel=LinearLayout(this);panel.orientation=LinearLayout.VERTICAL;panel.setPadding(dp(18),dp(16),dp(18),dp(16))
        panel.background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(0xE01B2640.toInt(),0xE010182B.toInt())).apply{cornerRadius=dp(22).toFloat();setStroke(dp(1),0xFF35405A.toInt())}
        parent.addView(panel,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(12)});return panel
    }
    private fun button(label:String,primary:Boolean=false,action:()->Unit) {
        val b=Button(this);b.text=label;b.isAllCaps=false;b.textSize=15f;b.typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL);b.setTextColor(if(primary)0xFF11231C.toInt() else ink);b.stateListAnimator=null
        val shape=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,if(primary)intArrayOf(0xFFE0FFAD.toInt(),0xFF9AE4BF.toInt())else intArrayOf(0xD0222D43.toInt(),0xD0182238.toInt())).apply{cornerRadius=dp(18).toFloat();if(!primary)setStroke(dp(1),0xFF35415A.toInt())}
        b.background=android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x337FFFFF),shape,null)
        root.addView(b,LinearLayout.LayoutParams(-1,dp(56)).apply{topMargin=dp(10)});b.setOnClickListener{if(!busy)action()}
    }
    private fun networkPills(p:Profile?=null) {
        val row=LinearLayout(this);row.gravity=Gravity.CENTER_VERTICAL;root.addView(row,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(8)})
        val network=LinearLayout(this);network.gravity=Gravity.CENTER_VERTICAL;network.setPadding(dp(10),dp(7),dp(10),dp(7));network.background=GradientDrawable().apply{setColor(0xA61C2539.toInt());cornerRadius=dp(12).toFloat();setStroke(dp(1),0xFF3C4460.toInt())}
        row.addView(network)
        val icon=ImageView(this);icon.setImageResource(R.drawable.solana_mark);icon.scaleType=ImageView.ScaleType.FIT_CENTER;icon.contentDescription="Solana"
        network.addView(icon,LinearLayout.LayoutParams(dp(20),dp(18)).apply{rightMargin=dp(8)});text("SOLANA",10,ink,network)
        val status=TextView(this);status.text=if(p?.seekerborn==true)"✦  SEEKERBORN" else "●  MAINNET";status.textSize=10f;status.setTextColor(lime);status.setPadding(dp(12),dp(7),dp(12),dp(7));row.addView(status)
    }
    private fun metrics(p:Profile) {
        val row=LinearLayout(this);root.addView(row)
        val left=card(row);left.layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{rightMargin=dp(6);bottomMargin=dp(12)}
        text(String.format(Locale.US,"%02d",p.traits.count{it.enabled}),30,lime,left);text("AWAKENED TRAITS",9,muted,left)
        val right=card(row);right.layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=dp(6);bottomMargin=dp(12)}
        text(String.format(Locale.US,"%02d",p.memories.size),30,ink,right);text("CONFIRMED MEMORIES",9,muted,right)
    }
    private fun resonanceCard(p:Profile) {
        val panel=card()
        text("SKR / RESONANCE",11,lime,panel)
        text(p.resonance.mode.title,24,ink,panel)
        text(SkrResonance.explanation(p),13,muted,panel)
        text("Held · ${amount(p.snapshot.skrBalance)} SKR\nStaked · ${amount(p.snapshot.stakedSkr)} SKR",14,lime,panel)
        if(p.resonance.earned)text("Resonance Mark + Core · earned permanently",12,lime,panel)
        p.resonance.firstObservedAt?.let{text("First SKR observation · "+date(it),11,muted,panel)}
        p.resonance.lastVerifiedAt?.let{text("Latest verified staking check · "+date(it),11,muted,panel)}
        val access=text(premiumEligibility(p).title+" ↗",13,lime,panel)
        access.setPadding(0,dp(12),0,0);panel.setOnClickListener{if(!busy)membership()}
    }
    private fun premiumStake(p:Profile):Double? {
        val live=CommerceSession.wallets[p.snapshot.address]?.takeIf{
            (System.currentTimeMillis()/1000-it.optLong("checkedAt")) in 0..300
        }
        return if(live==null)p.snapshot.stakedSkr else if(live.isNull("stakedSkr"))null else live.optDouble("stakedSkr")
    }
    private fun premiumEligibility(p:Profile)=PremiumPolicy.eligibility(premiumStake(p),
        CommerceSession.config?.optDouble("minimumStakeSkr",100.0) ?: 100.0)
    private fun membership() {
        val p=current ?: return welcome()
        val cfg=CommerceSession.config
        val minimum=cfg?.optDouble("minimumStakeSkr",100.0) ?: 100.0
        val eligibility=premiumEligibility(p)
        val verified=CommerceSession.wallets[p.snapshot.address]?.optJSONObject("premium")
        val paidStatus=verified?.optString("status")?.takeIf{it in setOf("ACTIVE","GRACE_PERIOD","EXPIRED","INACTIVE","UNKNOWN")}
        screen("Resonance Premium",paidStatus ?: eligibility.title,"30 days · tied to this wallet")
        val panel=card();text("ACTIVE STAKING",11,lime,panel)
        text(amount(premiumStake(p))+" / "+amount(minimum)+" SKR",28,ink,panel)
        text("Holding SKR does not count. A fresh server staking check is required before checkout.",13,muted,panel)
        text("Free Resonance starts above 0 staked SKR. Premium eligibility starts at "+amount(minimum)+" staked SKR. Amount never changes DNA or rarity.",14,lime)
        text("MVP · 100 SKR / 30 days. The live quote comes from your configured server. Future 3, 6 and 12 month discounted plans are not on sale.",14,ink)
        verified?.optLong("expiresAt")?.takeIf{it>0}?.let{text("Paid period ends · "+date(it),12,muted)}
        verified?.optLong("graceUntil")?.takeIf{it>0}?.let{text("Grace ends · "+date(it),12,muted)}
        space(12);text("Available Premium benefits",22,ink)
        text("HD PNG card · 2160 × 2880. Transparent avatar PNG · 1536 × 1536.",14,lime)
        if(CommerceSession.access(p.snapshot.address)){
            button("Export HD card"){premiumExport(false)}
            button("Export transparent avatar"){premiumExport(true)}
        }
        space(12);text("Planned benefits",22,ink)
        PremiumPolicy.benefits.forEach{benefit->
            val c=card();text(benefit.name+" ↗",20,ink,c);text(benefit.description,13,muted,c)
            c.setOnClickListener{if(!busy)premiumPreview(benefit)}
        }
        val price=cfg?.optString("premiumPriceSkr")
        if(cfg?.optBoolean("premiumAvailable")==true && !price.isNullOrBlank() && eligibility==MembershipEligibility.ELIGIBLE)
            button("Activate / renew · "+price+" SKR",true){purchase("premium")}
        button(if(commerceApi()==null)"Configure commerce endpoint" else "Refresh verified Premium status"){refreshCommerce()}
        if(getSharedPreferences("commerce",MODE_PRIVATE).contains("pending:"+p.snapshot.address))
            button("Resume payment verification"){recoverPurchase()}
        text("No HTTPS backend means no payment or paid access. 30 days from verified payment. Below the staking requirement: 7-day grace, bounded by paid expiry. Earned traits, data and free sharing remain available.",13,muted)
        button("Rescan staking"){scan(p.snapshot.address)}
        button("Explore Store"){cosmeticStore()}
        button("Back to creature",true){profile()}
    }
    private fun premiumPreview(benefit:PremiumBenefit) {
        val p=current ?: return welcome()
        screen("Premium preview",benefit.name,"Planned benefit · not included in this build")
        root.addView(CreatureView(this,p),LinearLayout.LayoutParams(-1,dp(240)))
        text(benefit.description,17,ink)
        text("This preview explains the planned feature. Premium checkout and this benefit are not available yet. Existing free memories, data and interactions remain available.",14,muted)
        button("Premium details",true){membership()}
    }
    private fun cosmeticStore() {
        activeTab="store"
        screen("Visual customization","Store","Cosmetics are separate from earned DNA")
        text("Store is available without Premium or staking. Only items with configured server prices can be purchased.",14,lime)
        CosmeticCatalog.items.groupBy{it.category}.forEach{(category,items)->
            space(12);text(category,22,ink)
            items.forEach{item->
                val panel=card();text(item.name+" ↗",20,ink,panel);text(item.description,13,muted,panel)
                val product=CommerceSession.config?.optJSONArray("items")?.objects()?.firstOrNull{it.optString("id")==item.id}
                text(if(product?.optBoolean("available")==true)"Preview · "+product.optString("priceSkr")+" SKR" else "Preview · price pending",11,lime,panel)
                panel.setOnClickListener{if(!busy)cosmeticPreview(item)}
            }
        }
        space(12);text("Accessories & animation packs",20,ink)
        text("Planned after visual review. No placeholder accessories are sold.",13,muted)
        button("Refresh Store prices"){refreshCommerce{cosmeticStore()}}
        button("My Items"){inventory()}
        button("Resonance Premium"){if(current==null)welcome() else membership()}
    }
    private fun cosmeticPreview(item:CosmeticItem) {
        val p=current
        if(p==null){android.app.AlertDialog.Builder(this).setTitle("Connect a wallet").setMessage("Preview cosmetics on your own creature after connecting.").setPositiveButton("Connect"){_,_->connect()}.setNegativeButton("Cancel",null).show();return}
        activeTab="store"
        screen(item.category,item.name,"Temporary preview · not purchased or equipped")
        root.addView(CosmeticPreview(this,p,item),LinearLayout.LayoutParams(-1,dp(380)))
        text(item.description,16,ink)
        val product=CommerceSession.config?.optJSONArray("items")?.objects()?.firstOrNull{it.optString("id")==item.id}
        if(item.id in CommerceSession.owned(p.snapshot.address)){
            val equipped=CosmeticEquipment.item(this,p.snapshot.address,item.category)?.id==item.id
            button(if(equipped)"Unequip" else "Equip"){equipCosmetic(item)}
        }
        else if(product?.optBoolean("available")==true && !product.isNull("priceSkr"))button("Buy · "+product.getString("priceSkr")+" SKR",true){purchase(item.id)}
        else text("Price pending · item is not for sale.",13,muted)
        text("Preview changes nothing in your DNA, earned traits, rarity or saved appearance.",13,muted)
        button("Cancel preview",true){cosmeticStore()}
    }
    private fun inventory() {
        activeTab="store";screen("Wallet-bound cosmetics","My Items","Purchased cosmetics belong to their purchasing wallet")
        val address=current?.snapshot?.address
        val items=address?.let{CommerceSession.owned(it)} ?: emptySet()
        if(items.isEmpty())text("No verified items loaded. Refresh from the server to restore purchases.",15,muted)
        CosmeticCatalog.items.filter{it.id in items}.forEach{item->
            val c=card();text(item.name,21,ink,c);text(item.category,12,muted,c)
            c.setOnClickListener{if(!busy)cosmeticPreview(item)}
        }
        button("Refresh verified inventory"){refreshCommerce{inventory()}}
        button("Browse Store",true){cosmeticStore()}
    }
    private fun commerceApi():CommerceApi? {
        val endpoint=getSharedPreferences("commerce",MODE_PRIVATE).getString("endpoint",null) ?: return null
        if(CommerceSession.endpoint!=endpoint){CommerceSession.reset();CommerceSession.endpoint=endpoint}
        return CommerceApi(endpoint)
    }
    private fun refreshCommerce(after:()->Unit={membership()}) {
        val api=commerceApi() ?: return settings();busy=true
        lifecycleScope.launch{
            try{
                CommerceSession.config=api.request("/v1/config")
                current?.snapshot?.address?.let{CommerceSession.wallets[it]=api.request("/v1/wallet?address=$it")}
                busy=false;after()
            }catch(e:CancellationException){throw e}catch(e:Exception){
                busy=false;Toast.makeText(this@MainActivity,e.message ?: "Commerce unavailable",Toast.LENGTH_LONG).show();after()
            }
        }
    }
    private fun purchase(item:String) {
        val p=current ?: return welcome();val api=commerceApi() ?: return settings()
        val prefs=getSharedPreferences("commerce",MODE_PRIVATE)
        if(prefs.contains("pending:"+p.snapshot.address)){recoverPurchase();return};busy=true
        lifecycleScope.launch{
            try{
                val raw=api.request("/v1/orders",org.json.JSONObject().put("wallet",p.snapshot.address).put("item",item))
                val order=CheckoutOrder.parse(raw)
                require(order.wallet==p.snapshot.address && order.item==item){"Order mismatch"}
                require(order.recipient=="6y8jfXZHHSYYaVLaEDnokW3MEKg1JfzA9S1Ld6zCv4SF"){"Unexpected merchant wallet"}
                require(java.math.BigDecimal(order.priceSkr).movePointRight(6).longValueExact()==order.amountUnits){"Price mismatch"}
                busy=false
                val detail=order.priceSkr+" SKR + Solana network fee\nFrom: "+order.wallet+"\nTo: "+order.recipient+
                    if(item=="premium")"\n30 days. Includes HD cards and transparent avatar export. Other benefits are planned." else "\nWallet-bound cosmetic."
                android.app.AlertDialog.Builder(this@MainActivity).setTitle("Confirm SKR purchase").setMessage(detail)
                    .setPositiveButton("Confirm in wallet"){_,_->sendPayment(api,raw,order)}.setNegativeButton("Cancel",null).show()
            }catch(e:CancellationException){throw e}catch(e:Exception){busy=false;Toast.makeText(this@MainActivity,e.message ?: "Cannot create order",Toast.LENGTH_LONG).show()}
        }
    }
    private fun sendPayment(api:CommerceApi,raw:org.json.JSONObject,order:CheckoutOrder) {
        busy=true
        lifecycleScope.launch{
            try{
                require(System.currentTimeMillis()/1000<order.expiresAt){"Order expired; request a fresh quote"}
                SkrPayment.validateAccounts(order,Rpc(store.rpc))
                val transaction=SkrPayment.transaction(order);raw.put("endpoint",api.endpoint)
                check(getSharedPreferences("commerce",MODE_PRIVATE).edit().putString("pending:"+order.wallet,raw.toString()).commit())
                val result=adapter.transact(sender){auth->
                    require(auth.accounts.any{Base58.encode(it.publicKey)==order.wallet}){"Choose the order's wallet"}
                    signAndSendTransactions(arrayOf(transaction))
                }
                when(result){
                    is TransactionResult.Success->{
                        val signature=result.successPayload?.signatures?.firstOrNull()?.let{Base58.encode(it)}
                            ?: throw IllegalStateException("Signature missing; resume using the transaction signature from your wallet")
                        raw.put("signature",signature)
                        check(getSharedPreferences("commerce",MODE_PRIVATE).edit().putString("pending:"+order.wallet,raw.toString()).commit())
                        busy=false;verifyPurchase(api,raw)
                    }
                    else->{busy=false;Toast.makeText(this@MainActivity,"Signature not returned. Pending order kept; check your wallet before paying again.",Toast.LENGTH_LONG).show();membership()}
                }
            }catch(e:CancellationException){throw e}catch(e:Exception){busy=false;Toast.makeText(this@MainActivity,e.message ?: "Payment unavailable",Toast.LENGTH_LONG).show();membership()}
        }
    }
    private fun verifyPurchase(api:CommerceApi,raw:org.json.JSONObject) {
        busy=true
        lifecycleScope.launch{
            try{
                val result=api.request("/v1/verify",org.json.JSONObject().put("orderId",raw.getString("id")).put("signature",raw.getString("signature")))
                require(result.optBoolean("verified") && result.getString("wallet")==raw.getString("wallet") && result.getString("item")==raw.getString("item")){"Receipt mismatch"}
                getSharedPreferences("commerce",MODE_PRIVATE).edit().remove("pending:"+raw.getString("wallet")).apply()
                val address=raw.getString("wallet")
                CommerceSession.wallets[address]=api.request("/v1/wallet?address=$address")
                busy=false;Toast.makeText(this@MainActivity,"SKR payment verified",Toast.LENGTH_LONG).show()
                if(raw.getString("item")=="premium")membership() else inventory()
            }catch(e:CancellationException){throw e}catch(e:Exception){
                busy=false;Toast.makeText(this@MainActivity,"Verification pending: "+e.message+". Resume verification; do not pay again.",Toast.LENGTH_LONG).show();membership()
            }
        }
    }
    private fun recoverPurchase() {
        val address=current?.snapshot?.address ?: return welcome()
        val prefs=getSharedPreferences("commerce",MODE_PRIVATE)
        val raw=org.json.JSONObject(prefs.getString("pending:$address",null) ?: return membership())
        val api=CommerceApi(raw.getString("endpoint"))
        if(raw.has("signature")){verifyPurchase(api,raw);return}
        val input=EditText(this);input.hint="Transaction signature from your wallet"
        android.app.AlertDialog.Builder(this).setTitle("Resume verification").setMessage("Paste the sent payment's signature. No new payment will be sent.")
            .setView(input).setPositiveButton("Verify"){_,_->
                val sig=input.text.toString().trim()
                if(runCatching{Base58.decode(sig).size==64}.getOrDefault(false)){
                    raw.put("signature",sig);prefs.edit().putString("pending:$address",raw.toString()).apply();verifyPurchase(api,raw)
                }else Toast.makeText(this,"Invalid signature",Toast.LENGTH_LONG).show()
            }.setNeutralButton("No payment sent"){_,_->
                android.app.AlertDialog.Builder(this).setTitle("Discard order?").setMessage("Only discard after checking that the wallet sent no payment.")
                    .setPositiveButton("Discard"){_,_->prefs.edit().remove("pending:$address").apply();membership()}.setNegativeButton("Keep",null).show()
            }.setNegativeButton("Keep pending",null).show()
    }
    private fun equipCosmetic(item:CosmeticItem) {
        val address=current?.snapshot?.address ?: return
        require(item.id in CommerceSession.owned(address))
        val prefs=getSharedPreferences("cosmetics",MODE_PRIVATE);val key="$address:"+item.category
        prefs.edit().apply{if(prefs.getString(key,null)==item.id)remove(key) else putString(key,item.id)}.apply()
        inventory()
    }
    private fun drawDisplayBackground(canvas:Canvas,p:Profile,w:Int,h:Int) {
        val item=CosmeticEquipment.item(this,p.snapshot.address,"Backgrounds")
        if(item==null)WalletkinBackdrop.draw(this,canvas,w.toFloat(),h.toFloat(),BackgroundEvolution.active(p,store.backgroundChoice(p.snapshot.address)).artKey)
        else CosmeticScene(this,item).apply{
            measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY))
            layout(0,0,w,h);draw(canvas)
        }
    }
    private fun premiumExport(transparent:Boolean) {
        val p=current ?: return
        if(!CommerceSession.access(p.snapshot.address)){refreshCommerce();return}
        try{
            val w=if(transparent)1536 else 2160;val h=if(transparent)1536 else 2880
            val bmp=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);val c=Canvas(bmp)
            if(transparent)CreatureView(this,p).render(c,p,w.toFloat(),h.toFloat())
            else {
                drawDisplayBackground(c,p,w,h);c.save();c.scale(2f,2f)
                val paint=Paint(Paint.ANTI_ALIAS_FLAG);paint.color=lime;paint.textSize=34f
                c.drawText("W A L L E T K I N",80f,100f,paint);paint.color=ink;paint.textSize=80f
                c.drawText(p.dna.species.name,80f,230f,paint)
                c.save();c.translate(0f,260f);CreatureView(this,p).render(c,p,1080f,810f);c.restore()
                paint.textSize=36f;c.drawText(p.traits.count{it.enabled}.toString()+" traits · "+p.memories.size+" memories",80f,1140f,paint)
                paint.textSize=28f;paint.color=muted;c.drawText("Genesis DNA · "+p.dna.seed,80f,1210f,paint)
                c.drawText(p.resonance.mode.title,80f,1280f,paint);c.restore()
            }
            val file=File(File(cacheDir,"cards").apply{mkdirs()},if(transparent)"walletkin-avatar.png" else "walletkin-hd.png")
            file.outputStream().use{bmp.compress(Bitmap.CompressFormat.PNG,100,it)};bmp.recycle()
            val uri=FileProvider.getUriForFile(this,"$packageName.files",file)
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="image/png";putExtra(Intent.EXTRA_STREAM,uri);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)},"Share Premium export"))
        }catch(e:Exception){Toast.makeText(this,"Export unavailable; retry",Toast.LENGTH_LONG).show()}
    }
    private fun traitPreview(entry:LegendEntry) {
        activeTab="legends";screen("Visual preview",entry.name,"Six trait integrations · illustrative, not wallet evidence")
        val seed=CanonicalWorld.from("11111111111111111111111111111111")
        val sample=Evolution.derive(Snapshot("11111111111111111111111111111111",0.0,null,null,null,0,emptyList(),0,emptyList(),false))
            .copy(dna=seed.copy(species=CanonicalWorld.byId(entry.id)))
        root.addView(CreatureView(this,sample,true),LinearLayout.LayoutParams(-1,dp(360)))
        text("Resonance Mark/Core · Active Resonance · Seekerborn/Genesis Core · Jupiter Navigator · JUP Orbit Keeper · NFT Collector",15,lime)
        text("Preview only. Real creatures display earned traits from confirmed evidence; unavailable checks stay unknown. This preview is never saved to My Colony.",13,muted)
        button("Back to lineage",true){legend(entry)}
    }

    private fun currentBackground():BackgroundTrait=BackgroundEvolution.active(current,current?.let{store.backgroundChoice(it.snapshot.address)})
    private fun backgroundCard(p:Profile) {
        val realm=BackgroundEvolution.active(p,store.backgroundChoice(p.snapshot.address))
        val panel=card();val label=text("REALM  /  BACKGROUND TRAIT",10,lime,panel);label.letterSpacing=0.10f
        text(realm.name,22,ink,panel);text(realm.reason,12,muted,panel)
        val choose=text("${BackgroundEvolution.unlocked(p).size} unlocked · Change realm ↗",12,lime,panel);choose.setPadding(0,dp(10),0,0);panel.setOnClickListener{if(!busy)chooseBackground(p)}
    }
    private fun chooseBackground(p:Profile) {
        val choices=BackgroundEvolution.unlocked(p)
        val labels=arrayOf("Automatic · ${BackgroundEvolution.active(p).name}")+choices.map{it.name}.toTypedArray()
        android.app.AlertDialog.Builder(this).setTitle("Choose your realm").setItems(labels){_,index->store.chooseBackground(p.snapshot.address,if(index==0)null else choices[index-1].id);profile()}.show()
    }
    private fun walletMenu() {
        val labels=if(current==null)arrayOf("Connect wallet","Connection settings")else arrayOf("Share creature","Connect another wallet","Reconnect wallet","Connection settings","Disconnect wallet")
        android.app.AlertDialog.Builder(this).setTitle("Walletkin").setItems(labels){_,index->when(labels[index]){
            "Share creature"->current?.let{share(it)};"Connect another wallet"->{adapter.authToken=null;connect()};"Reconnect wallet","Connect wallet"->connect();"Connection settings"->settings();else->disconnect()
        }}.show()
    }
    private fun legends() {
        activeTab="legends";screen("The living archive","Legends","Thirteen lineages. One anomaly. Every origin has a story.")
        val entries=LegendsCatalog.read(this)
        if(entries.isEmpty()) {
            val intro=card();text("13 + 1",38,lime,intro);text("The archive is taking shape",20,ink,intro);text("Discover each lineage's origin, character and visual identity. The Anomaly holds several forms within one legend.",14,muted,intro)
        } else {
            entries.forEachIndexed { index,entry ->
                val panel=card();text(if(entry.anomaly)"✧  THE ANOMALY" else "LINEAGE ${String.format(Locale.US,"%02d",index+1)}",10,lime,panel)
                text(entry.name,26,ink,panel);text(entry.subtitle,13,muted,panel);panel.addView(CanonicalSceneView(this,entry.id,portrait=true),LinearLayout.LayoutParams(-1,dp(220)))
                panel.setOnClickListener{legend(entry)}
            }
        }
    }
    private fun legend(entry:LegendEntry) {
        if (entry.anomaly) { anomalyForm(entry); return }
        activeTab="legends";screen(if(entry.anomaly)"Anomaly" else "Lineage",entry.name,entry.subtitle)
        root.addView(CanonicalSceneView(this,entry.id),LinearLayout.LayoutParams(-1,dp(340)))
        text("Tap to interact · basic MVP animation",12,muted);space(16)
        button("Preview six visual traits"){traitPreview(entry)}
        text("COLOR FORMS",10,lime)
        CanonicalWorld.palettes.forEachIndexed { index,name -> button(name){legendColor(entry,index)} }
        val story=card();text(entry.lore,17,ink,story);text("MVP: idle, blink and tap reactions. Planned for the full product: richer poses, emotions and wallet-event choreography.",12,muted,story)
        if(entry.forms.isNotEmpty()){space(10);text("KNOWN FORMS",10,lime);entry.forms.forEach{val panel=card();text(it,19,ink,panel)}}
        button("Back to Legends",true){legends()}
    }
    private fun legendColor(entry:LegendEntry,palette:Int) {
        activeTab="legends";screen("Color form",entry.name,CanonicalWorld.palettes[palette])
        root.addView(CanonicalSceneView(this,entry.id,palette),LinearLayout.LayoutParams(-1,dp(340)))
        text("Preview color. Your wallet inherits its own stable DNA color.",13,muted)
        CanonicalWorld.palettes.forEachIndexed { index,name -> button(name){legendColor(entry,index)} }
        button("Back to lineage",true){legend(entry)}
    }
    private fun anomalyForm(entry: LegendEntry, index: Int = 0) {
        val ids = listOf("dachshund", "manta", "octopus", "fractal", "snail")
        val selected = index.coerceIn(ids.indices)
        activeTab = "legends"
        screen("Anomaly", entry.forms[selected], entry.subtitle)
        root.addView(CanonicalSceneView(this, "anomaly/${ids[selected]}"),
            LinearLayout.LayoutParams(-1, dp(340)))
        text("Tap to interact", 12, muted)
        entry.forms.forEachIndexed { position, name ->
            button(if (position == selected) "• $name" else name) { anomalyForm(entry, position) }
        }
        val story = card(); text(entry.lore, 17, ink, story)
        button("Back to Legends", true) { legends() }
    }

    private fun welcome() {
        activeTab="creature";screen("Your wallet. Alive.","Meet your Walletkin","A living chronicle shaped by real on-chain history."); primaryScreen=true
        val sample=Evolution.derive(Snapshot("11111111111111111111111111111111",0.0,null,null,null,0,emptyList(),0,emptyList(),false))
        networkPills();root.addView(CreatureView(this,sample),LinearLayout.LayoutParams(-1,dp(290)))
        text("One address. One Genesis DNA.\nEvery confirmed footprint can leave a mark.",16,ink)
        button("Connect wallet",true){connect()}; button("My Colony · ${store.addresses().size}"){colony()}; button("Connection settings"){settings()}
        space(18); text("Read-only mainnet analysis. Walletkin never asks for a seed phrase. Purchases require explicit wallet confirmation.",12,muted)
    }
    private fun connect() {
        busy=true; screen("Wallet connection","Open your wallet","Approve Walletkin in an MWA-compatible wallet, such as Seed Vault Wallet.")
        lifecycleScope.launch {
            try {
                when(val result=adapter.connect(sender)) {
                    is TransactionResult.Success -> {
                        val address=Base58.encode(result.authResult.accounts.first().publicKey)
                        getSharedPreferences("session",MODE_PRIVATE).edit().putString("auth",adapter.authToken).putString("last",address).apply()
                        busy=false; scan(address,true)
                    }
                    is TransactionResult.NoWalletFound -> {busy=false; error("No compatible wallet found. Open Seed Vault Wallet on your Seeker, then try again.")}
                    is TransactionResult.Failure -> {busy=false; error("Connection was cancelled or could not complete. Your Colony is safe.")}
                }
            } catch(e:CancellationException){throw e} catch(e:Exception){busy=false;error("Could not connect. Please retry in your wallet.")}
        }
    }
    private fun scan(address:String,reveal:Boolean=false) {
        val old=store.load(address); busy=true
        screen("Reading your chronicle","Listening to the chain","Genesis DNA is fixed. We only change what confirmed evidence supports.")
        val status=text("Starting scan…",16,lime); val progress=ProgressBar(this);root.addView(progress);space(16)
        lifecycleScope.launch {
            try {
                val snap=withTimeout(180000) { Analyzer(Rpc(store.rpc)).scan(address,old?.snapshot){s -> runOnUiThread { status.text=s }} }
                val next=Evolution.derive(snap,old?.seekerborn ?: false,old?.resonance ?: ResonanceState())
                withContext(Dispatchers.IO){store.save(next)};current=next;busy=false
                getSharedPreferences("session",MODE_PRIVATE).edit().putString("last",address).apply()
                if(old == null || reveal) reveal(next) else {
                    val fresh=Evolution.newMemories(old,next)
                    val gained=next.traits.filter{it.enabled && old.traits.none{o -> o.id==it.id && o.enabled}}
                    if(fresh.isNotEmpty() || gained.isNotEmpty() || (old.resonance.mode!=next.resonance.mode && next.resonance.mode!=ResonanceMode.UNKNOWN)) mutation(fresh.size,gained) else {profile();Toast.makeText(this@MainActivity,if(next.resonance.mode==ResonanceMode.UNKNOWN)"Staking check unavailable. Your earned marks are preserved." else "No new confirmed footprints. Your Walletkin is unchanged.",Toast.LENGTH_LONG).show()}
                }
            } catch(e:TimeoutCancellationException){busy=false;current=old;error("The scan timed out. Your saved profile is preserved. Try a dedicated mainnet RPC."){scan(address,reveal)}} catch(e:CancellationException){throw e} catch(e:Exception){busy=false;current=old;error("The scan could not complete. The previous saved profile was preserved. Check your connection or choose a dedicated mainnet RPC."){scan(address,reveal)}}
        }
    }
    private fun reveal(p:Profile) {
        screen("Genesis revealed",p.dna.species.name,p.dna.species.subtitle)
        val creature=CreatureView(this,p);root.addView(creature,LinearLayout.LayoutParams(-1,dp(310)));creature.alpha=0f;creature.scaleX=0.8f;creature.scaleY=0.8f;creature.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(900).start()
        text("DNA v2 · ${p.dna.seed} · ${CanonicalWorld.palettes[p.dna.palette]}",13,lime);space(12);text("${p.traits.count{it.enabled}} awakened traits · ${p.memories.size} confirmed memories",16)
        button("Meet your Walletkin",true){profile()}
    }
    private fun mutation(count:Int,gained:List<Trait>) {
        val p=current ?: return
        screen("New evidence detected","Your Walletkin is changing…","$count newly observed memories · ${gained.size} newly awakened traits")
        val view=CreatureView(this,p);root.addView(view,LinearLayout.LayoutParams(-1,dp(310)));view.alpha=0f;view.animate().alpha(1f).setDuration(1100).start()
        text(p.resonance.mode.title,16,lime)
        gained.forEach{text("✦ ${it.name}",16,lime)}
        BackgroundEvolution.unlocked(p).filter{realm->gained.any{it.id==realm.requiredTrait}}.forEach{text("Realm awakened · ${it.name}",14,lime)}
        text(SkrResonance.explanation(p),13,muted)
        text("Newly observed memories may include older transactions that were outside the previous scan. Each footprint adds a glowing mote.",12,muted)
        button("See the new chapter",true){profile()}
    }
    private fun profile() {
        val p=current ?: return welcome()
        activeTab="creature";screen(if(p.seekerborn)"Seekerborn · permanent crest" else "Genesis v2",p.dna.species.name,p.dna.species.subtitle); primaryScreen=true
        networkPills(p)
        root.addView(CreatureView(this,p),LinearLayout.LayoutParams(-1,dp(320)))
        text(CanonicalWorld.palettes[p.dna.palette]+" · inherited origin color",12,muted)
        if(store.takeMigrationNotice())text("Your creature now belongs to the Legends world. Your wallet memories are preserved.",13,lime)
        val identity=text(short(p.snapshot.address),12,muted);identity.gravity=Gravity.CENTER;space(16)
        metrics(p);resonanceCard(p);backgroundCard(p)
        text("Last scanned ${date(p.snapshot.scannedAt)}",11,muted)
        if(p.snapshot.warnings.isNotEmpty()){space(8);text(p.snapshot.warnings.joinToString("\n"),12,0xFFFFC38D.toInt())}
        button("Rescan · discover new memories",true){scan(p.snapshot.address)}
        button("Lore · Story / Data"){lore()};button("Traits & Memories"){memories()};button("Share your Walletkin"){share(p)};
    }
    private fun lore(data:Boolean=false) {
        val p=current ?: return
        screen("Wallet biography",if(data)"The evidence" else "The story",if(data)"Every trait has an observable reason." else "Your history, translated into a living identity.")
        if(data) {
            text("Address\n${p.snapshot.address}\n\nGenesis v2 · ${p.dna.seed}\nSOL · ${amount(p.snapshot.sol)}\nSKR · ${amount(p.snapshot.skrBalance)}\nStaked SKR · ${amount(p.snapshot.stakedSkr)}\nActive JUP governance stake · ${amount(p.snapshot.stakedJup)} JUP\nSGT currently held · ${p.snapshot.seeker?.toString() ?: "unknown"}\nSeekerborn saved · ${p.seekerborn}\nToken kinds · ${p.snapshot.tokenKinds ?: "Unknown"}\nConfirmed successful details · ${p.memories.size}",14)
            text("Resonance · "+p.resonance.mode.title+"\nPermanent SKR Mark/Core · "+p.resonance.earned+"\nMembership · "+premiumEligibility(p).title,14,lime)
            space(16);text("Scan scope: newest 100 signatures; up to 20 unseen transaction details per scan, accumulated locally. History ${if(p.snapshot.historyLimited)"is partial" else "fits the current window"}. No wallet-age or complete-history claim is made.",13,muted)
        } else text(Evolution.story(p),18)
        button(if(data)"Read Story" else "See Data"){lore(!data)};button("Back to profile",true){profile()}
    }
    private fun memories() {
        val p=current ?: return
        screen("A chronicle in motion","Traits & Memories","15 evidence-based traits. Memories preserve real transaction timestamps.")
        p.traits.forEach { val c=card();text("${if(it.enabled)"✦" else "○"} ${it.name}",17,if(it.enabled)lime else muted,c);text(it.reason,12,muted,c) }
        space(16);text("MEMORIES",12,lime)
        if(p.memories.isEmpty())text("No confirmed footprints observed yet.",15,muted)
        p.memories.asReversed().take(100).forEach { m -> val c=card();text(m.title,16,ink,c);text(m.time?.let{date(it)} ?: "Timestamp unavailable",12,muted,c);val link=text("View transaction ↗",12,lime,c);link.setOnClickListener{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://solscan.io/tx/${m.signature}"))) } }
        button("Back to profile",true){profile()}
    }
    private fun colony() {
        activeTab="colony";screen("Your constellation","My Colony","Saved wallet identities stay on this device.")
        store.addresses().forEach { address -> store.load(address)?.let{p -> val c=card();text(p.dna.species.name,21,ink,c);text(short(address),12,muted,c);text(premiumEligibility(p).title,12,lime,c);val creature=CreatureView(this,p);c.addView(creature,LinearLayout.LayoutParams(-1,dp(160)));c.setOnClickListener{current=p;profile()};c.setOnLongClickListener{android.app.AlertDialog.Builder(this).setTitle("Remove this local profile?").setMessage("You can reveal it again by reconnecting this wallet.").setPositiveButton("Remove"){_,_->store.remove(address);if(current?.snapshot?.address==address)current=null;colony()}.setNegativeButton("Keep",null).show();true} } }
        if(store.addresses().isEmpty())text("Your first Walletkin is waiting to be revealed.",17,muted)
        button("Connect wallet",true){adapter.authToken=null;connect()};button("Back"){if(current==null)welcome() else profile()}
    }
    private fun settings() {
        screen("Connection","Mainnet RPC","A dedicated endpoint makes wallet-history scans more reliable. It is stored only on this device.")
        val input=EditText(this);input.setText(store.rpc);input.setTextColor(ink);input.textSize=14f;input.inputType=android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI;root.addView(input)
        text("Use a Solana mainnet HTTPS endpoint. Keep provider credentials private. Public RPC can rate-limit scans.",13,muted)
        button("Save endpoint",true){try{val value=input.text.toString().trim();require(Uri.parse(value).scheme=="https" && !Uri.parse(value).host.isNullOrBlank());store.rpc=value;Toast.makeText(this,"Endpoint saved",Toast.LENGTH_SHORT).show();if(current==null)welcome() else profile()}catch(e:Exception){input.error="Enter a valid HTTPS mainnet RPC URL"}}
        space(16);text("Commerce server",20,lime)
        val commerceInput=EditText(this);commerceInput.setText(getSharedPreferences("commerce",MODE_PRIVATE).getString("endpoint",""));commerceInput.hint="https://your-commerce-server";commerceInput.setTextColor(ink);root.addView(commerceInput)
        text("Trusted deployed HTTPS backend. Empty endpoint keeps payments disabled.",13,muted)
        button("Save commerce endpoint"){
            try{
                val value=commerceInput.text.toString().trim();if(value.isNotEmpty())CommerceApi(value)
                getSharedPreferences("commerce",MODE_PRIVATE).edit().apply{if(value.isEmpty())remove("endpoint") else putString("endpoint",value)}.apply()
                CommerceSession.reset();Toast.makeText(this,"Commerce endpoint saved",Toast.LENGTH_SHORT).show()
                if(current==null)welcome() else membership()
            }catch(e:Exception){commerceInput.error="Use a trusted HTTPS URL"}
        }
        button("Back"){if(current==null)welcome() else profile()}
    }
    private fun disconnect(){busy=true;lifecycleScope.launch{try{val r=adapter.disconnect(sender);if(r is TransactionResult.Success){getSharedPreferences("session",MODE_PRIVATE).edit().remove("auth").remove("last").apply();adapter.authToken=null;current=null;busy=false;welcome()}else{busy=false;error("Wallet authorization was not revoked. Retry disconnect in your wallet.")}}catch(e:CancellationException){throw e}catch(e:Exception){busy=false;error("Could not revoke wallet authorization. Retry disconnect.")}}}
    private fun error(message:String,retry:(()->Unit)?=null){screen("Connection paused","Let's try again",message);if(retry!=null)button("Retry scan",true,retry);button("My Colony"){colony()};button("Connection settings"){settings()};button("Back"){if(current==null)welcome() else profile()}}
    private fun share(p:Profile) {
        try {
            val bmp=Bitmap.createBitmap(1080,1440,Bitmap.Config.ARGB_8888);val c=Canvas(bmp);drawDisplayBackground(c,p,1080,1440);val paint=Paint(Paint.ANTI_ALIAS_FLAG);paint.color=lime;paint.textSize=34f;c.drawText("W A L L E T K I N",80f,100f,paint);paint.color=ink;paint.textSize=80f;c.drawText(p.dna.species.name,80f,230f,paint)
            c.save();c.translate(0f,260f);CreatureView(this,p).render(c,p,1080f,810f);c.restore();paint.textSize=36f;paint.color=ink;c.drawText("${p.traits.count{it.enabled}} traits · ${p.memories.size} memories",80f,1140f,paint);paint.textSize=28f;paint.color=muted;c.drawText("Genesis DNA · ${p.dna.seed}",80f,1210f,paint);c.drawText(p.resonance.mode.title,80f,1280f,paint)
            val directory=File(cacheDir,"cards").apply{mkdirs()};val file=File(directory,"walletkin.png");file.outputStream().use{bmp.compress(Bitmap.CompressFormat.PNG,100,it)};bmp.recycle();val uri=FileProvider.getUriForFile(this,"$packageName.files",file)
            val intent=Intent(Intent.ACTION_SEND).apply{type="image/png";putExtra(Intent.EXTRA_STREAM,uri);putExtra(Intent.EXTRA_TEXT,"Meet my ${p.dna.species.name}. My wallet. Alive. #Walletkin");addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)};startActivity(Intent.createChooser(intent,"Share your Walletkin"))
        } catch(e:Exception){Toast.makeText(this,"Could not create share card. Please retry.",Toast.LENGTH_LONG).show()}
    }
    private fun short(a:String)=a.take(6)+"…"+a.takeLast(6)
    private fun amount(v:Double?)=v?.let{String.format(Locale.US,"%.3f",it)} ?: "Unknown"
    private fun date(time:Long)=SimpleDateFormat("dd MMM yyyy · HH:mm",Locale.getDefault()).format(Date(time*1000))
}





