package presentation.code_reader

import presentation.code_reader.Entity.KeyWord.*
import ru.icames.store.presentation.code_reader.AccountingMode

class Entity {

    /**
     * Enumerating of keys that accepted for reading
     * @param keyWord name of accepted key
     * @param valuesCount min count of params that can be in scan data.
     * By default count of values can be any
     *
     * */

    enum class KeyWord(val keyWord: String, val valuesCount: Int = 2, val description: String) {
        USER("user", 5, "Сотрудник"),
        STORE_CELL("store-cell", 3, "Ячейка"),
        DETAIL("detail", description = "Деталь"),
        PATTERN("pattern", description = "Плита"),
        OFFCUT("offcut", description = "Остаток"),
        PACK("pack", description = "Упаковка"),
        EDGE("edge", description = "Кромка"),
        PALLET("pallet", description = "Паллет"),
        LINEAR("linear", description = "Погонаж"),
        BOARD("board", description = "Плита")
    }

    /**
     * Lists of accepted keys for every accounting mode
     * */

    companion object {

        private val acceptedAll = arrayOf(USER, STORE_CELL, DETAIL, PATTERN, OFFCUT, PACK, EDGE, BOARD, LINEAR, PALLET)

        private val acceptedAuth = arrayOf(USER)

        private val acceptedStoreCell = arrayOf(STORE_CELL)

        private val acceptedObject = arrayOf(DETAIL, PATTERN, OFFCUT, PACK, EDGE, BOARD, LINEAR, PALLET)


        /**
         *  Find keyword in list of all accepted keys
         *  @param keyWord name of finding key
         * */
        fun find(keyWord: String): KeyWord? {
            return acceptedAll.find {
                it.keyWord == keyWord
            }
        }

        /**
         *  Check is key accepted for concrete accounting mode
         *  @param value name of finding key
         *  @param accountingMode checking accounting mode
         * */
        fun contains(value: String, accountingMode: AccountingMode): Boolean {

           val accepted = when(accountingMode){
                AccountingMode.OBJECT -> acceptedObject
                AccountingMode.ALL -> acceptedAll
                AccountingMode.AUTH -> acceptedAuth
                AccountingMode.STORE_CELL -> acceptedStoreCell
           }

            for(i in accepted){
                if(i.keyWord == value) {
                    return true
                }
            }
            return false
        }

    }
}