package utils;

public class BlackLists {
    public static final String[] brandBlackList = new String[] {
            "Богатырь",
            "MIDr",
            "Drawinchi",
            "Арт узор",
            "Первые семена",
            "Mottomo",
            "AQUA",
            " Brand University",
            "FitStars",
            "Ковчег",
            "Вышивка оптом",
            "Издательство Учитель",
            "Сестричество Игнатия Ставропольского",
            "Издательство Сретенского монастыря",
            "Христианская жизнь",
            "Иоанно-Богословский Савво-Крыпецкий монастырь",
            "Духовное преображение",
            "Сибирская Благозвонница",
            "МериЛу",
            "Да!Маск",
            "Pappi Store",
            "Новое Небо",
            "За пару минут",
            "Свято-Троицкая Сергиева Лавра",
            "Издательство Белорусского Экзархата",
            "Скрижаль",
            "Navigator",
            "Wolta",
            "KD",
            "Comix",
            "HOBBY LINE",
            "Проспект",
            "СОЮЗ",
            "Sopra",
            "Giulia",
            "А М Дизайн",
            "РУСФЛАГ",
            "NB Nabil"
    };
    public static final String[] categoryBlackList = new String[] {

    };

    private static boolean contains(String[] blackList, String item) {
        for (String s : blackList) {
            if (s.equals(item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsInBrandBL(String brand) {
        return contains(brandBlackList, brand);
    }

    public static boolean containsInCategoryBL(String category) {
        return contains(categoryBlackList, category);
    }

}
