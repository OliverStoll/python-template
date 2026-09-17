package com.oliverstoll.sobriety;

import java.util.ArrayList;
import java.util.List;

public final class Icons {

    private static final Icon[] ALL_ICONS = {
            // Alcohol & drinks
            new Icon("🍺", "beer", "alcohol", "drink", "pint"),
            new Icon("🍻", "beers", "alcohol", "drink", "cheers"),
            new Icon("🍷", "wine", "alcohol", "drink", "red wine", "white wine"),
            new Icon("🍸", "cocktail", "drink", "alcohol", "margarita"),
            new Icon("🍹", "tropical drink", "alcohol", "drink", "fruity"),
            new Icon("🍾", "bottle", "champagne", "alcohol", "drink", "sparkling"),
            new Icon("🥂", "champagne", "toast", "alcohol", "drink", "celebration"),
            new Icon("🥃", "whiskey", "liquor", "alcohol", "drink"),
            new Icon("🍶", "sake", "alcohol", "drink", "rice wine"),
            new Icon("☕", "coffee", "caffeine", "drink", "espresso"),
            new Icon("🫖", "teapot", "tea", "drink", "caffeine"),
            new Icon("🧃", "juice box", "drink", "sugar", "fruit juice"),
            new Icon("🥤", "soda", "energy drink", "sugar", "drink", "cola"),
            new Icon("🧋", "bubble tea", "drink", "sugar", "boba"),
            new Icon("🧉", "drink", "mate", "tea", "caffeine"),

            // Smoking & tobacco
            new Icon("🚬", "cigarette", "smoke", "nicotine", "tobacco", "smoking"),
            new Icon("🚭", "no smoking", "quit", "stop", "forbidden"),

            // Food & sweets
            new Icon("🍰", "cake", "gebäck", "pastries", "baked goods", "sweet", "sugar", "dessert"),
            new Icon("🎂", "birthday cake", "cake", "gebäck", "dessert", "sweet"),
            new Icon("🧁", "cupcake", "gebäck", "pastries", "baked goods", "sweet", "muffin"),
            new Icon("🥐", "croissant", "gebäck", "pastries", "baked goods", "french"),
            new Icon("🍪", "cookie", "gebäck", "pastries", "baked goods", "snack"),
            new Icon("🍩", "donut", "gebäck", "pastries", "sweet", "sugar", "fried"),
            new Icon("🍫", "chocolate", "candy", "sugar", "sweet", "cocoa"),
            new Icon("🍬", "candy", "lollipop", "sugar", "sweet", "zucker", "sweets"),
            new Icon("🍭", "candy", "sugar", "zucker", "sweets", "lollipop"),
            new Icon("🍮", "pudding", "dessert", "sweet", "sugar", "flan"),
            new Icon("🍯", "honey pot", "sweet", "sugar", "honey"),
            new Icon("🍡", "dango", "dessert", "sweet", "japanese"),
            new Icon("🍪", "cookie", "biscuit", "snack", "sweet"),
            new Icon("🥧", "pie", "baked", "dessert", "sweet", "gebäck"),
            new Icon("🧀", "cheese", "dairy", "food"),
            new Icon("🍔", "burger", "fast food", "junk food", "eating", "meat"),
            new Icon("🍟", "fries", "fast food", "junk food", "potato", "salt"),
            new Icon("🌭", "hotdog", "food", "junk food"),
            new Icon("🌮", "taco", "food", "mexican"),
            new Icon("🌯", "burrito", "food", "mexican", "wrap"),
            new Icon("🥪", "sandwich", "food", "bread", "lunch"),
            new Icon("🥙", "pita", "food", "bread", "greek"),
            new Icon("🥗", "salad", "healthy", "diet", "vegetables", "green"),
            new Icon("🥘", "paella", "food", "cooking", "spanish"),
            new Icon("🍝", "pasta", "food", "italian", "noodles"),
            new Icon("🍜", "ramen", "noodles", "food", "asian"),
            new Icon("🍲", "soup", "food", "hot", "broth"),
            new Icon("🥟", "dumpling", "food", "asian", "dim sum"),
            new Icon("🍛", "curry", "food", "asian", "spicy"),
            new Icon("🍣", "sushi", "food", "japanese", "healthy"),
            new Icon("🍱", "bento", "food", "japanese", "meal"),
            new Icon("🥞", "pancakes", "breakfast", "sweet", "food"),
            new Icon("🍞", "bread", "food", "carbs"),
            new Icon("🥐", "croissant", "breakfast", "food", "bakery"),
            new Icon("🥨", "pretzel", "snack", "food", "salty"),
            new Icon("🧈", "butter", "dairy", "cooking"),
            new Icon("🍎", "apple", "fruit", "healthy", "diet"),
            new Icon("🍊", "orange", "fruit", "healthy", "vitamin c"),
            new Icon("🍋", "lemon", "fruit", "sour", "healthy"),
            new Icon("🍌", "banana", "fruit", "healthy", "potassium"),
            new Icon("🍉", "watermelon", "fruit", "healthy", "summer"),
            new Icon("🍇", "grapes", "fruit", "healthy", "wine"),
            new Icon("🍓", "strawberry", "fruit", "healthy", "sweet"),
            new Icon("🍈", "melon", "fruit", "healthy"),
            new Icon("🍒", "cherries", "fruit", "healthy", "sweet"),
            new Icon("🍑", "peach", "fruit", "healthy", "sweet"),
            new Icon("🥥", "coconut", "fruit", "tropical", "healthy"),
            new Icon("🥑", "avocado", "fruit", "healthy", "fat"),
            new Icon("🍅", "tomato", "vegetable", "food", "healthy"),
            new Icon("🍆", "eggplant", "vegetable", "food", "masturbieren"),
            new Icon("🌶️", "pepper", "spicy", "vegetable", "food"),
            new Icon("🌽", "corn", "vegetable", "food"),
            new Icon("🥒", "pickle", "vegetable", "food", "cucumber"),
            new Icon("🥬", "leafy greens", "vegetable", "healthy", "diet"),
            new Icon("🥦", "broccoli", "vegetable", "healthy", "diet", "green"),
            new Icon("🧄", "garlic", "vegetable", "cooking"),
            new Icon("🧅", "onion", "vegetable", "cooking"),
            new Icon("🥔", "potato", "vegetable", "food", "carbs"),
            new Icon("🍠", "sweet potato", "vegetable", "food", "healthy"),

            // Gambling & games
            new Icon("🎰", "gambling", "casino", "bet", "slots", "luck"),
            new Icon("🎲", "dice", "game", "gambling", "luck", "random"),
            new Icon("🃏", "cards", "gambling", "poker", "game", "bet"),
            new Icon("🎴", "mahjong", "game", "gambling", "asian"),
            new Icon("🎮", "gaming", "video games", "game", "console", "controller"),
            new Icon("🕹️", "joystick", "gaming", "video games", "arcade"),
            new Icon("♟️", "chess", "game", "strategy", "board game"),

            // Internet & screens
            new Icon("📱", "phone", "screen", "social", "media", "internet", "porn", "masturbieren", "mobile"),
            new Icon("💻", "laptop", "screen", "computer", "work", "internet", "porn"),
            new Icon("🖥️", "desktop", "computer", "screen", "work", "internet", "porn"),
            new Icon("⌨️", "keyboard", "computer", "work", "typing"),
            new Icon("🖱️", "mouse", "computer", "work"),
            new Icon("📺", "tv", "screen", "entertainment", "porn", "video"),
            new Icon("📞", "phone", "call", "communication", "device"),
            new Icon("☎️", "telephone", "communication", "call"),
            new Icon("📲", "phone", "text", "message", "sms"),
            new Icon("💾", "floppy disk", "save", "storage", "old", "computer"),
            new Icon("💿", "cd", "disc", "media", "storage"),
            new Icon("📀", "dvd", "disc", "media", "video"),

            // Entertainment
            new Icon("🎬", "movies", "entertainment", "video", "porn", "cinema", "film"),
            new Icon("🎥", "camera", "video", "recording", "film", "movie"),
            new Icon("📹", "video camera", "recording", "video", "film"),
            new Icon("🎞️", "film strip", "movie", "cinema", "entertainment"),
            new Icon("📽️", "film projector", "movie", "cinema", "old"),
            new Icon("🎭", "theater", "entertainment", "drama", "acting"),
            new Icon("🎪", "circus", "entertainment", "fun", "show"),
            new Icon("🎨", "art", "painting", "creativity", "drawing"),
            new Icon("🎬", "film", "video", "entertainment", "porn"),
            new Icon("📚", "books", "reading", "education", "learning", "study", "library"),
            new Icon("📖", "open book", "reading", "education", "study"),
            new Icon("📕", "red book", "reading", "book", "education"),
            new Icon("📗", "green book", "reading", "book", "education"),
            new Icon("📘", "blue book", "reading", "book", "education"),
            new Icon("📙", "yellow book", "reading", "book", "education"),
            new Icon("📓", "notebook", "writing", "notes", "education"),
            new Icon("📔", "notebook", "writing", "notes", "diary"),
            new Icon("📒", "ledger", "notes", "writing"),
            new Icon("📰", "newspaper", "news", "reading", "information"),
            new Icon("🗞️", "newspaper", "news", "reading"),

            // Music & sound
            new Icon("🎵", "music", "sound", "note", "song"),
            new Icon("🎶", "music", "sound", "notes", "song", "melody"),
            new Icon("🎤", "microphone", "singing", "music", "voice"),
            new Icon("🎧", "headphones", "music", "sound", "audio", "listening"),
            new Icon("🎼", "musical score", "music", "composition"),
            new Icon("🎹", "piano", "music", "instrument", "keyboard"),
            new Icon("🥁", "drum", "music", "percussion", "rhythm"),
            new Icon("🎸", "guitar", "music", "instrument", "rock"),
            new Icon("🎺", "trumpet", "music", "instrument", "jazz"),
            new Icon("🎷", "saxophone", "music", "instrument", "jazz"),
            new Icon("🎻", "violin", "music", "instrument", "classical"),

            // Exercise & fitness
            new Icon("💪", "exercise", "fitness", "gym", "strength", "muscle", "strong"),
            new Icon("🏋️", "weightlifting", "fitness", "gym", "exercise", "strength"),
            new Icon("🤸", "acrobatics", "fitness", "flexibility", "exercise"),
            new Icon("⛹️", "basketball", "sports", "fitness", "exercise", "game"),
            new Icon("🏃", "running", "exercise", "fitness", "sport", "jogging"),
            new Icon("🚴", "cycling", "exercise", "fitness", "bike", "sport"),
            new Icon("🏊", "swimming", "exercise", "fitness", "water", "sport"),
            new Icon("🧗", "climbing", "exercise", "fitness", "sport", "adventure"),
            new Icon("⛷️", "skiing", "sport", "winter", "exercise"),
            new Icon("🏂", "snowboarding", "sport", "winter", "exercise"),
            new Icon("🤾", "handball", "sport", "exercise", "game"),
            new Icon("⛹️", "ball", "sport", "exercise", "basketball"),
            new Icon("🤺", "fencing", "sport", "exercise", "sword"),
            new Icon("🥊", "boxing", "sport", "exercise", "fighting"),
            new Icon("🥋", "karate", "martial arts", "exercise", "fighting"),
            new Icon("🎿", "skiing", "sport", "winter", "exercise"),
            new Icon("🥌", "curling", "sport", "winter", "ice"),

            // Mental health & wellness
            new Icon("🧘", "meditation", "yoga", "mindfulness", "relaxation", "peace", "calm"),
            new Icon("🧠", "mind", "mental health", "therapy", "brain", "thinking"),
            new Icon("❤️", "health", "heart", "love", "care", "wellness"),
            new Icon("💚", "heart", "health", "love", "wellness", "green"),
            new Icon("💙", "heart", "health", "love", "wellness", "blue"),
            new Icon("💛", "heart", "health", "love", "wellness", "yellow"),
            new Icon("🧡", "heart", "health", "love", "wellness", "orange"),
            new Icon("💜", "heart", "health", "love", "wellness", "purple"),

            // Work & productivity
            new Icon("💼", "work", "job", "career", "productivity", "business", "briefcase"),
            new Icon("📊", "progress", "tracking", "statistics", "chart", "data", "analytics"),
            new Icon("📈", "chart", "progress", "growth", "statistics", "analytics"),
            new Icon("📉", "chart", "statistics", "decline", "analytics"),
            new Icon("📋", "clipboard", "task", "list", "notes", "memo"),
            new Icon("📝", "writing", "notes", "document", "memo", "task"),
            new Icon("✏️", "pencil", "writing", "note", "drawing"),
            new Icon("✒️", "pen", "writing", "note"),
            new Icon("🖊️", "pen", "writing", "note", "ballpoint"),
            new Icon("🖋️", "fountain pen", "writing", "note"),

            // Nature & outdoors
            new Icon("🌳", "nature", "outdoor", "fresh air", "exercise", "tree", "forest"),
            new Icon("🌲", "tree", "nature", "forest", "outdoor", "pine"),
            new Icon("🌴", "palm tree", "nature", "tropical", "outdoor"),
            new Icon("🌵", "cactus", "nature", "desert", "outdoor"),
            new Icon("🌾", "grain", "nature", "harvest", "outdoor"),
            new Icon("🌿", "plant", "nature", "green", "fresh"),
            new Icon("☘️", "clover", "nature", "luck", "green"),
            new Icon("🍀", "four leaf clover", "luck", "nature", "green"),
            new Icon("🎋", "bamboo", "nature", "asian", "plant"),
            new Icon("🎍", "pine decoration", "nature", "japanese", "new year"),
            new Icon("⛅", "cloud", "weather", "outdoor", "fresh air", "nature"),
            new Icon("⛈️", "storm", "weather", "rain", "thunder"),
            new Icon("🌤️", "sunny", "weather", "outdoor", "clear"),
            new Icon("🌥️", "cloudy", "weather", "outdoor"),
            new Icon("🌦️", "rainy", "weather", "outdoor", "rain"),
            new Icon("🌧️", "rain", "weather", "outdoor", "wet"),
            new Icon("☀️", "morning", "day", "sunshine", "bright", "energy"),
            new Icon("🌙", "night", "sleep", "rest", "moon", "dark"),
            new Icon("⭐", "star", "bright", "night", "wishes"),
            new Icon("✨", "sparkle", "magic", "general", "sobriety", "bright", "shine"),
            new Icon("🌟", "glowing star", "bright", "night", "success"),
            new Icon("💫", "dizzy", "stars", "night", "sparkle"),
            new Icon("⚡", "lightning", "energy", "power", "electricity", "fast"),
            new Icon("❄️", "snowflake", "winter", "cold", "fresh"),
            new Icon("💧", "droplet", "water", "rain", "fresh", "clean"),
            new Icon("💦", "water spray", "wet", "splash", "fresh"),
            new Icon("☔", "umbrella", "rain", "weather", "protection"),
            new Icon("🌊", "wave", "water", "ocean", "beach", "nature"),
            new Icon("🏖️", "beach", "vacation", "outdoor", "relax"),
            new Icon("⛱️", "umbrella", "beach", "vacation", "relax"),
            new Icon("🏝️", "island", "vacation", "outdoor", "nature"),

            // Time & routine
            new Icon("⏰", "time", "clock", "schedule", "routine", "alarm", "morning"),
            new Icon("⏱️", "stopwatch", "time", "timer", "track"),
            new Icon("⏲️", "timer", "time", "clock", "cooking"),
            new Icon("🕰️", "clock", "time", "vintage", "schedule"),
            new Icon("📅", "calendar", "date", "schedule", "plan"),
            new Icon("📆", "calendar", "date", "schedule"),
            new Icon("🗓️", "calendar", "date", "schedule", "plan"),
            new Icon("📇", "card index", "organize", "task", "list"),
            new Icon("⌛", "hourglass", "time", "waiting", "running out"),
            new Icon("⏳", "hourglass", "time", "sand", "waiting"),

            // Goals & achievement
            new Icon("🎯", "goal", "target", "focus", "achievement", "aim"),
            new Icon("🏆", "achievement", "success", "win", "trophy", "first place"),
            new Icon("🥇", "first place", "gold medal", "achievement", "winner"),
            new Icon("🥈", "second place", "silver medal", "achievement"),
            new Icon("🥉", "third place", "bronze medal", "achievement"),
            new Icon("🎖️", "medal", "achievement", "honor"),
            new Icon("🏅", "medal", "achievement", "sports", "honor"),

            // Education
            new Icon("🎓", "education", "learning", "school", "graduation", "degree"),
            new Icon("🎒", "backpack", "school", "student", "education"),
            new Icon("📚", "books", "learning", "study", "education", "library"),

            // Miscellaneous positive
            new Icon("💤", "sleep", "rest", "night", "zzzz", "tired", "recovery"),
            new Icon("👍", "thumbs up", "good", "positive", "success", "approval"),
            new Icon("👊", "fist", "power", "strength", "motivation"),
            new Icon("✊", "fist", "power", "solidarity", "strength"),
            new Icon("🤝", "handshake", "agreement", "support", "help"),
            new Icon("🙏", "pray", "gratitude", "thanks", "hope"),
            new Icon("🎊", "confetti", "celebration", "success", "party"),
            new Icon("🎉", "party", "celebration", "success", "joy"),
            new Icon("🎈", "balloon", "celebration", "party", "joy"),
    };

    public static class Icon {
        public final String emoji;
        public final String[] keywords;

        public Icon(String emoji, String... keywords) {
            this.emoji = emoji;
            this.keywords = keywords;
        }

        public boolean matches(String query) {
            if (query == null || query.isEmpty()) return true;
            String lower = query.toLowerCase();
            for (String keyword : keywords) {
                if (keyword.contains(lower)) return true;
            }
            return false;
        }
    }

    public static List<Icon> search(String query) {
        List<Icon> results = new ArrayList<>();
        for (Icon icon : ALL_ICONS) {
            if (icon.matches(query)) {
                results.add(icon);
            }
        }
        return results;
    }

    public static List<Icon> all() {
        List<Icon> all = new ArrayList<>();
        for (Icon icon : ALL_ICONS) {
            all.add(icon);
        }
        return all;
    }

    private Icons() {}
}
