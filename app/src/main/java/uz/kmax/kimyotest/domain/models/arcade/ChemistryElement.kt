package uz.kmax.kimyotest.domain.models.arcade

/**
 * Mendeleyev jadvalidagi kimyoviy elementni ifodalaydi.
 * @param atomicNumber - Elementning tartib raqami (Z). O'yin logikasida kalit.
 * @param symbol - Elementning kimyoviy belgisi (masalan, "H", "He", "Li").
 * @param nameUz - O'zbek tilidagi nomi.
 * @param color - Tile uchun fon rangi (ARGB format).
 */
data class ChemistryElement(
    val atomicNumber: Int,
    val symbol: String,
    val nameUz: String,
    val color: Long
)

/**
 * O'yindagi 11 darajali elementlar zanjiri.
 * Har bir bosqich Z = oldingisining 2 barobari.
 * H(1) -> He(2) -> Be(4) -> O(8) -> S(16) -> Ge(32) ->
 * Nd(64) -> Ds(128) -> Fl(256) -> Ts(512) -> Og(1024) -> Lr(2048)
 */
object ChemistryElementTable {

    val ELEMENTS = mapOf(
        1    to ChemistryElement(1,    "H",  "Vodorod",        0xFF78C8F0L),
        2    to ChemistryElement(2,    "He", "Geliy",          0xFF90D4A0L),
        4    to ChemistryElement(4,    "Be", "Berilliy",       0xFFF0C040L),
        8    to ChemistryElement(8,    "O",  "Kislorod",       0xFFF08040L),
        16   to ChemistryElement(16,   "S",  "Oltingugurt",    0xFFE05050L),
        32   to ChemistryElement(32,   "Ge", "Germaniy",       0xFFB040C0L),
        64   to ChemistryElement(64,   "Nd", "Neodimiy",       0xFF8040E0L),
        128  to ChemistryElement(128,  "Ds", "Darmshtadtiy",   0xFF6030D0L),
        256  to ChemistryElement(256,  "Fl", "Fleroviy",       0xFF4020C0L),
        512  to ChemistryElement(512,  "Ts", "Tennessiy",      0xFF301890L),
        1024 to ChemistryElement(1024, "Og", "Oganesson",      0xFF201060L),
        2048 to ChemistryElement(2048, "Lr", "Lorensiy",       0xFF100830L)
    )

    fun getElement(atomicNumber: Int): ChemistryElement? = ELEMENTS[atomicNumber]

    fun isWinningElement(atomicNumber: Int) = atomicNumber >= 2048

    fun getTextColor(atomicNumber: Int): Long =
        if (atomicNumber <= 4) 0xFF333333L else 0xFFFFFFFFL
}
