package com.example.utils

import android.content.Context
import android.content.Intent
import com.example.data.database.entity.*

object ExportHelper {

    fun exportCapitalTransactionsToCsv(list: List<CapitalTransactionEntity>): String {
        val sb = StringBuilder()
        sb.append("ID Transaksi,Tanggal,Tipe,Nominal,Sumber/Tujuan,Keterangan,User,Dibuat\n")
        list.forEach { item ->
            sb.append("\"${item.transactionId}\",")
            sb.append("\"${Formatters.formatDate(item.transactionDate)}\",")
            sb.append("\"${item.type}\",")
            sb.append("${item.amount},")
            sb.append("\"${item.sourceOrTarget}\",")
            sb.append("\"${item.notes.replace("\"", "\"\"")}\",")
            sb.append("\"${item.createdBy}\",")
            sb.append("\"${Formatters.formatDateTime(item.createdAt)}\"\n")
        }
        return sb.toString()
    }

    fun exportGoldIntakesToCsv(list: List<GoldIntakeEntity>): String {
        val sb = StringBuilder()
        sb.append("ID Intake,Tanggal,No Tromol,Pengolahan,Berat (Gram),Kadar,Harga/Gram,Biaya Pengolahan,Nilai Kotor,Potongan,Nilai Bersih/Potong Modal,User\n")
        list.forEach { item ->
            sb.append("\"${item.intakeId}\",")
            sb.append("\"${Formatters.formatDate(item.transactionDate)}\",")
            sb.append("\"${item.batchNumber}\",")
            sb.append("\"${item.processingName}\",")
            sb.append("${item.weightGrams},")
            sb.append("\"${item.kadarName}\",")
            sb.append("${item.pricePerGram},")
            sb.append("${item.processingFee},")
            sb.append("${item.grossValue},")
            sb.append("${item.deductionAmount},")
            sb.append("${item.netValue},")
            sb.append("\"${item.createdBy}\"\n")
        }
        return sb.toString()
    }

    fun exportBatchesToCsv(list: List<ProcessingBatchEntity>): String {
        val sb = StringBuilder()
        sb.append("No Tromol,Nama Pengolahan,Pengelola,Tgl Masuk,Tgl Selesai,Berat Bahan (g),Biaya Pengolahan,Status,Keterangan\n")
        list.forEach { item ->
            sb.append("\"${item.batchNumber}\",")
            sb.append("\"${item.batchName}\",")
            sb.append("\"${item.managerName}\",")
            sb.append("\"${Formatters.formatDate(item.startDate)}\",")
            sb.append("\"${if (item.endDate != null) Formatters.formatDate(item.endDate) else "-"}\",")
            sb.append("${item.rawMaterialWeight},")
            sb.append("${item.processingFee},")
            sb.append("\"${item.status}\",")
            sb.append("\"${item.notes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    fun exportInventoryToCsv(list: List<GoldInventoryEntity>): String {
        val sb = StringBuilder()
        sb.append("ID Inventory,ID Intake,Tgl Masuk,No Tromol,Kadar,Berat Awal (g),Berat Tersedia (g),Berat Terjual (g),Modal/Gram,Total Modal,Status,Lokasi\n")
        list.forEach { item ->
            sb.append("\"${item.inventoryId}\",")
            sb.append("\"${item.intakeId}\",")
            sb.append("\"${Formatters.formatDate(item.entryDate)}\",")
            sb.append("\"${item.batchNumber}\",")
            sb.append("\"${item.kadarName}\",")
            sb.append("${item.initialWeightGrams},")
            sb.append("${item.availableWeightGrams},")
            sb.append("${item.soldWeightGrams},")
            sb.append("${item.costPerGram},")
            sb.append("${item.totalCostValue},")
            sb.append("\"${item.status}\",")
            sb.append("\"${item.location}\"\n")
        }
        return sb.toString()
    }

    fun exportSalesToCsv(list: List<GoldSalesEntity>): String {
        val sb = StringBuilder()
        sb.append("ID Penjualan,Tanggal,ID Inv,No Tromol,Kadar,Berat Terjual (g),Harga Jual/g,Total Penjualan,Modal Pokok,Biaya Terkait,Laba Bersih,Pembeli,User\n")
        list.forEach { item ->
            sb.append("\"${item.salesId}\",")
            sb.append("\"${Formatters.formatDate(item.transactionDate)}\",")
            sb.append("\"${item.inventoryId}\",")
            sb.append("\"${item.batchNumber}\",")
            sb.append("\"${item.kadarName}\",")
            sb.append("${item.weightSoldGrams},")
            sb.append("${item.sellPricePerGram},")
            sb.append("${item.totalSalesAmount},")
            sb.append("${item.costOfGoodsSold},")
            sb.append("${item.relatedExpense},")
            sb.append("${item.profitAmount},")
            sb.append("\"${item.buyerName}\",")
            sb.append("\"${item.createdBy}\"\n")
        }
        return sb.toString()
    }

    fun exportActivityLogsToCsv(list: List<ActivityLogEntity>): String {
        val sb = StringBuilder()
        sb.append("ID Log,Waktu,User,Role,Tindakan,No Transaksi,Rincian\n")
        list.forEach { item ->
            sb.append("${item.id},")
            sb.append("\"${Formatters.formatDateTime(item.timestamp)}\",")
            sb.append("\"${item.username}\",")
            sb.append("\"${item.userRole}\",")
            sb.append("\"${item.actionType}\",")
            sb.append("\"${item.transactionId ?: ""}\",")
            sb.append("\"${item.details.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }
}
