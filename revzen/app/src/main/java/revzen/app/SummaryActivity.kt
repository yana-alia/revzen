package revzen.app

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import revzen.app.api.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class SummaryActivity : AppCompatActivity() {
    private lateinit var apiHandler: ApiHandler
    private lateinit var studyList: ArrayList<SessionData>
    private lateinit var studyRes: ApiHandler.Reward

    private lateinit var xp: TextView
    private lateinit var totalStudy: TextView
    private lateinit var totalBreak: TextView
    private lateinit var ratio: TextView
    private lateinit var petXP: TextView

    private lateinit var petImage: ImageView
    private lateinit var healthImage: ImageView


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        studyList = intent.extras?.getParcelableArrayList("studyList")!!
        apiHandler = intent.extras?.getParcelable("handler")!!

        xp = findViewById(R.id.summaryXP)
        totalStudy = findViewById(R.id.summaryTotalStudy)
        totalBreak = findViewById(R.id.summaryTotalBreak)
        ratio = findViewById(R.id.summaryRatio)
        petXP = findViewById(R.id.summaryPetXP)

        petImage = findViewById(R.id.summaryPetImage)
        healthImage = findViewById(R.id.summaryPetHealth)

        studyRes = calculateResult(studyList)

        xp.text = "${studyRes.xpGained} XP"
        totalStudy.text = timeFormat(studyRes.total_study_time)
        totalBreak.text = timeFormat(studyRes.total_break_time)
        if (studyRes.total_break_time == 0) {
            ratio.text = "N/A"
        } else {
            val ratioVal =
                studyRes.total_study_time.toDouble() / studyRes.total_break_time.toDouble()
            ratio.text = "${(ratioVal * 100.0).roundToInt() / 100.0}"
        }

        apiHandler.stopLiveRevision({}, { })

        val random = Random()
        if (random.nextInt(GIVE_PET_CHANCE) == 1 && studyRes.healthChange >= 0) {
            apiHandler.givePet(
                Pet.values()[random.nextInt(Pet.values().size - 1) + 1],
                this::successfulGivePet,
                this::givePetFailure
            )
        } else {
            apiHandler.giveReward(studyRes, this::successfulReward, this::rewardFailure)
        }
    }

    override fun onBackPressed() {}

    fun goToMenu(_view: View) {
        finish()
    }

    @SuppressLint("SetTextI18n")
    fun successfulReward(reward: RewardResponse) {

        if (reward.petChange != PetChange.NoChange) {
            AlertDialog.Builder(this).apply {
                setTitle("Pet Died")
                setIcon(reward.pet.logoImage)
                setMessage(
                    when (reward.petChange) {
                        PetChange.OnlyRock -> "Your pet has died, and you are now only left with the rock"
                        PetChange.SwitchedPet -> "Your pet has died, so you have now switched pet to ${reward.pet.petName}"
                        else -> "No pet change has occurred"
                    }
                )
                setPositiveButton("Ok") { _, _ -> }
                create()
                show()
            }
        }

        petImage.setImageResource(reward.pet.logoImage)
        if (reward.pet != Pet.ROCK) {
            healthImage.setImageResource(reward.health.image)
            healthImage.visibility = View.VISIBLE
            petXP.text = "${reward.XP} XP"
            petXP.visibility = View.VISIBLE
        }
        petImage.visibility = View.VISIBLE
    }

    private fun rewardFailure(error: ApiError) {
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage(
                when (error) {
                    ApiError.NO_SUCH_USER -> R.string.login_failure_no_such_user
                    ApiError.WRONG_VERSION -> R.string.login_failure_outdated_api
                    else -> R.string.login_failure_unspecified_api_error
                }
            )
            setPositiveButton("Ok") { _, _ -> finish() }
            create()
            show()
        }
    }

    private fun successfulGivePet(give_result: GiveResult) {

        AlertDialog.Builder(this).apply {
            setIcon(give_result.pet.logoImage)
            if (give_result.isNew) {
                setTitle("Gained a new Pet!")
                setMessage("You have gained a new pet ${give_result.pet.petName} for your family! Study well & keep it healthy!")
            } else {
                setTitle("Health Boost!")
                setMessage("Boosted ${give_result.pet.petName} to full health")
            }
            setPositiveButton("Ok") { _, _ -> }
            create()
            show()
        }
        apiHandler.giveReward(studyRes, this::successfulReward, this::rewardFailure)
    }

    private fun givePetFailure(error: ApiError) {
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage(
                when (error) {
                    ApiError.NO_SUCH_USER -> R.string.login_failure_no_such_user
                    ApiError.WRONG_VERSION -> R.string.login_failure_outdated_api
                    else -> R.string.login_failure_unspecified_api_error
                }
            )
            setPositiveButton("Ok") { _, _ -> finish() }
            create()
            show()
        }
    }
}