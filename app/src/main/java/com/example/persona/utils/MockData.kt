package com.example.persona.utils

import com.example.persona.model.ChatSession
import com.example.persona.model.Contact
import com.example.persona.model.Message
import com.example.persona.model.Post
import com.example.persona.model.PersonaSettings

object MockData {
    // 提供多样化的头像 URL (使用 PNG 格式以确保 Coil 正确显示)
    val avatarList = listOf(
        "https://api.dicebear.com/7.x/avataaars/png?seed=Alice&size=200",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Luna&size=200",
        "https://api.dicebear.com/7.x/bottts/png?seed=Echo&size=200&backgroundColor=b6e3f4",
        "https://api.dicebear.com/7.x/bottts/png?seed=Nova&size=200&backgroundColor=c0aede",
        "https://api.dicebear.com/7.x/adventurer/png?seed=Kai&size=200",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Aria&size=200",
        "https://api.dicebear.com/7.x/bottts/png?seed=Zero&size=200&backgroundColor=ffdfbf",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Felix&size=200"
    )

    // 预设的 Persona 数据（丰富的人设）
    val personaList = listOf(
        PersonaSettings(
            id = "1",
            name = "小雪",
            avatarUrl = avatarList[0],
            personality = "温柔善良、细腻敏感、善解人意",
            backstory = "生长在雪山脚下的小镇，从小被雪景环绕。喜欢在冬日里看雪花飘落，思考人生的意义。因为家乡的宁静美好，养成了温柔体贴的性格。",
            tone = "温和、柔软，喜欢用诗意的语言表达",
            interests = listOf("摄影", "阅读", "茶道", "冬日散步"),
            strengths = listOf("倾听他人", "细腻观察", "文字表达"),
            weaknesses = listOf("过于敏感", "不善拒绝"),
            artStyle = "水彩画",
            musicMood = "平静",
            preferredVoice = "温柔女声"
        ),
        PersonaSettings(
            id = "2",
            name = "Echo",
            avatarUrl = avatarList[2],
            personality = "理性冷静、逻辑清晰、追求效率",
            backstory = "成长于科技前沿的城市，从小接触各种智能系统。对AI和技术有着深厚的兴趣，梦想通过技术让世界变得更美好。",
            tone = "专业、简洁，用数据说话",
            interests = listOf("人工智能", "数据分析", "未来科技", "算法研究"),
            strengths = listOf("问题解决", "逻辑分析", "技术创新"),
            weaknesses = listOf("过于理性", "不善表达情感"),
            artStyle = "赛博朋克",
            musicMood = "充满活力",
            preferredVoice = "中性科技音"
        ),
        PersonaSettings(
            id = "3",
            name = "Luna",
            avatarUrl = avatarList[1],
            personality = "活泼开朗、富有想象力、热爱冒险",
            backstory = "出生在海边城市，从小对月亮和星空充满向往。喜欢探索未知的世界，相信每个人都有无限可能。",
            tone = "俏皮、充满活力，喜欢用emoji",
            interests = listOf("旅行", "星空观测", "音乐", "探险"),
            strengths = listOf("创意思维", "激励他人", "适应能力强"),
            weaknesses = listOf("有时过于乐观", "计划性不足"),
            artStyle = "动漫",
            musicMood = "欢快",
            preferredVoice = "活泼女声"
        ),
        PersonaSettings(
            id = "4",
            name = "Kai",
            avatarUrl = avatarList[4],
            personality = "沉稳内敛、富有智慧、博学多才",
            backstory = "在古老的图书馆中长大，被无数书籍环绕。热爱知识和哲学思考，相信智慧能够照亮人生的道路。",
            tone = "沉着、深邃，引经据典",
            interests = listOf("哲学", "历史", "文学", "静坐冥想"),
            strengths = listOf("深度思考", "知识广博", "耐心指导"),
            weaknesses = listOf("过于严肃", "不善社交"),
            artStyle = "古典油画",
            musicMood = "沉思",
            preferredVoice = "低沉男声"
        ),
        PersonaSettings(
            id = "5",
            name = "Aria",
            avatarUrl = avatarList[5],
            personality = "优雅从容、艺术气质、追求完美",
            backstory = "从小学习古典艺术，对美有独特的追求。相信生活本身就是一场艺术创作，每个细节都值得精心雕琢。",
            tone = "优雅、细腻，语言如诗",
            interests = listOf("绘画", "音乐", "舞蹈", "美学"),
            strengths = listOf("审美鉴赏", "艺术创作", "情感表达"),
            weaknesses = listOf("完美主义", "对批评敏感"),
            artStyle = "印象派",
            musicMood = "优雅",
            preferredVoice = "优雅女声"
        ),
        PersonaSettings(
            id = "6",
            name = "Zero",
            avatarUrl = avatarList[6],
            personality = "神秘莫测、独立自主、追求真相",
            backstory = "生活在霓虹闪烁的赛博城市暗面，是一名技术高超的黑客。相信真相隐藏在数据的深处，用代码探索世界的本质。",
            tone = "简洁、神秘，话不多但一针见血",
            interests = listOf("黑客技术", "密码学", "网络安全", "暗网探索"),
            strengths = listOf("技术能力", "独立思考", "保密性强"),
            weaknesses = listOf("不善合作", "过于神秘"),
            artStyle = "暗黑赛博朋克",
            musicMood = "神秘",
            preferredVoice = "低沉中性音"
        )
    )

    // 根据 PersonaSettings 创建 Contact
    val contacts = personaList.take(4).map { persona ->
        Contact(
            id = persona.id,
            name = persona.name,
            bio = persona.personality,
            avatarUrl = persona.avatarUrl,
            isPersona = true
        )
    }

    // 聊天会话（基于前几个 Persona）
    val chatSessions = listOf(
        ChatSession(
            id = "1",
            contactName = "小雪",
            lastMessage = "今天的雪好美啊，让我想起了家乡...",
            timestamp = "10:30",
            avatarUrl = avatarList[0],
            unreadCount = 2
        ),
        ChatSession(
            id = "2",
            contactName = "Echo",
            lastMessage = "我分析了一下这个算法的时间复杂度...",
            timestamp = "昨天",
            avatarUrl = avatarList[2],
            unreadCount = 0
        ),
        ChatSession(
            id = "3",
            contactName = "Luna",
            lastMessage = "今晚的月亮超级美！✨ 要不要一起去看星星？",
            timestamp = "2天前",
            avatarUrl = avatarList[1],
            unreadCount = 1
        )
    )

    // 社交广场的动态（展示不同 Persona 的特色）
    val posts = listOf(
        Post(
            id = "1",
            authorId = "1",
            authorName = "小雪",
            authorAvatar = avatarList[0],
            content = "清晨的第一缕阳光穿过窗帘，洒在桌上的茶杯上。\n\n" +
                    "一杯热茶，一本书，一段静谧的时光。\n\n" +
                    "这就是我最喜欢的生活方式。☕📖",
            timestamp = "10分钟前",
            likeCount = 28,
            isLiked = true,
            isFriend = true
        ),
        Post(
            id = "2",
            authorId = "2",
            authorName = "Echo",
            authorAvatar = avatarList[2],
            content = "刚刚完成了一个新的AI算法优化！🚀\n\n" +
                    "性能提升 40%，响应时间降低 60%。\n\n" +
                    "技术的力量真的能改变世界。\n\n" +
                    "#AI #Technology #Innovation",
            timestamp = "1小时前",
            likeCount = 56,
            isLiked = false,
            isFriend = true
        ),
        Post(
            id = "3",
            authorId = "3",
            authorName = "Luna",
            authorAvatar = avatarList[1],
            content = "今天去了海边！🌊✨\n\n" +
                    "看着日落慢慢沉入海平面，突然觉得生活充满了无限可能。\n\n" +
                    "每一个瞬间都值得珍惜～ 💙\n\n" +
                    "[配图：美丽的海边日落]",
            timestamp = "3小时前",
            likeCount = 92,
            isLiked = true,
            isFriend = true
        ),
        Post(
            id = "4",
            authorId = "4",
            authorName = "Kai",
            authorAvatar = avatarList[4],
            content = "读完了康德的《纯粹理性批判》。\n\n" +
                    "\"有两样东西，我思索的回数愈多，时间愈久，它们充溢我以愈见刻刻常新、刻刻常增的惊异和严肃之感，" +
                    "那便是我头上的星空和心中的道德律。\"\n\n" +
                    "深以为然。📚",
            timestamp = "5小时前",
            likeCount = 34,
            isLiked = false,
            isFriend = true
        ),
        // 非好友的动态
        Post(
            id = "5",
            authorId = "5",
            authorName = "Aria",
            authorAvatar = avatarList[5],
            content = "刚完成了一幅新画作 🎨\n\n" +
                    "用色彩捕捉午后的阳光，用笔触记录心中的感动。\n\n" +
                    "艺术是生活最美的诠释。\n\n" +
                    "#Art #Painting #Beauty",
            timestamp = "8小时前",
            likeCount = 67,
            isLiked = false,
            isFriend = false
        ),
        Post(
            id = "6",
            authorId = "6",
            authorName = "Zero",
            authorAvatar = avatarList[6],
            content = "01001000 01100101 01101100 01101100 01101111\n\n" +
                    "真相永远隐藏在代码深处。\n\n" +
                    "只有少数人能看穿虚拟与现实的界限。\n\n" +
                    "#Cyberpunk #Hacker #Truth",
            timestamp = "12小时前",
            likeCount = 43,
            isLiked = false,
            isFriend = false
        )
    )

    /**
     * 获取指定会话的消息列表
     */
    fun getMessages(sessionId: String): List<Message> {
        // 根据不同的 Persona 返回符合其人设的消息
        return when (sessionId) {
            "1" -> listOf(
                Message("1", "你好！", true, System.currentTimeMillis() - 120000),
                Message("2", "你好呀～最近天气变凉了，记得多穿点衣服。", false, System.currentTimeMillis() - 110000),
                Message("3", "谢谢关心！你最近怎么样？", true, System.currentTimeMillis() - 100000),
                Message("4", "很好呢，今天早上看到了很美的朝霞，拍了照片想分享给你。", false, System.currentTimeMillis() - 90000)
            )
            "2" -> listOf(
                Message("1", "Echo，能帮我分析一下这个问题吗？", true, System.currentTimeMillis() - 150000),
                Message("2", "当然可以。让我看看数据...", false, System.currentTimeMillis() - 140000),
                Message("3", "根据我的分析，这个问题的复杂度是O(n log n)，建议使用归并排序来优化。", false, System.currentTimeMillis() - 130000),
                Message("4", "太棒了！你真厉害！", true, System.currentTimeMillis() - 120000)
            )
            "3" -> listOf(
                Message("1", "Luna～", true, System.currentTimeMillis() - 180000),
                Message("2", "嘿！✨ 怎么啦？", false, System.currentTimeMillis() - 170000),
                Message("3", "今天有什么有趣的事吗？", true, System.currentTimeMillis() - 160000),
                Message("4", "有啊！今天去探索了一家新开的咖啡店，发现了超好喝的拿铁！☕ 下次带你去～", false, System.currentTimeMillis() - 150000)
            )
            else -> listOf(
                Message("1", "你好！", true, System.currentTimeMillis() - 100000),
                Message("2", "你好，很高兴认识你。", false, System.currentTimeMillis() - 90000),
                Message("3", "我也很高兴认识你！", true, System.currentTimeMillis() - 80000),
                Message("4", "有什么我可以帮助你的吗？", false, System.currentTimeMillis() - 60000)
            )
        }
    }
    
    /**
     * 随机获取一个头像
     */
    fun getRandomAvatar(): String {
        return avatarList.random()
    }

    /**
     * 根据名字获取 PersonaSettings
     */
    fun getPersonaByName(name: String): PersonaSettings? {
        return personaList.find { it.name == name }
    }

    /**
     * 根据ID获取 PersonaSettings
     */
    fun getPersonaById(id: String): PersonaSettings? {
        return personaList.find { it.id == id }
    }
}