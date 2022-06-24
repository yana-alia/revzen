package revzen.app

enum class Pet(val logoImage: Int,
               val studyImage: Int,
               val breakImage: Int,
               val failImage:Int) {
    SHIBA(R.drawable.petlogo_shiba,R.drawable.petstudy_shiba, R.drawable.petbreak_shiba, R.drawable.petfail_shiba),
    HUSKY(R.drawable.petlogo_husky,R.drawable.petstudy_husky, R.drawable.petbreak_husky, R.drawable.petfail_husky),
    CALICO(R.drawable.petlogo_calico,R.drawable.petstudy_calico, R.drawable.petbreak_calico, R.drawable.petfail_calico),
    ROCK(R.drawable.petlogo_rock,R.drawable.petstudy_rock, R.drawable.petbreak_rock, R.drawable.petfail_rock),
}
