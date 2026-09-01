package com.example.data.model

enum class UserRole(val displayName: String) {
    OWNER("Owner / Pemilik"),
    ADMIN("Admin Operasional"),
    OPERATOR("Operator Lapangan"),
    VIEWER("Hanya Melihat (Viewer)");

    fun canManageCapital(): Boolean = this == OWNER
    fun canManageUsers(): Boolean = this == OWNER
    fun canDeleteTransactions(): Boolean = this == OWNER
    fun canViewProfitLoss(): Boolean = this == OWNER || this == ADMIN
    fun canManageMasterPrices(): Boolean = this == OWNER || this == ADMIN
    fun canManageTromol(): Boolean = this != VIEWER
    fun canInputIntake(): Boolean = this != VIEWER
    fun canInputSales(): Boolean = this != VIEWER
    fun canExportReports(): Boolean = this == OWNER || this == ADMIN
}

enum class BatchStatus {
    PROSES,
    SELESAI,
    DIBATALKAN
}

enum class CapitalTransactionType {
    TAMBAH_MODAL,
    PENGURANGAN_MODAL,
    PENGAMBILAN_EMAS_USAGE,
    PENJUALAN_EMAS_REVENUE
}

enum class InventoryStatus {
    TERSEDIA,
    SEBAGIAN_TERJUAL,
    TERJUAL,
    RUSAK,
    HILANG,
    LAINNYA
}

enum class ActionType {
    LOGIN,
    TAMBAH_MODAL,
    PENGURANGAN_MODAL,
    TAMBAH_TROMOL,
    UBAH_TROMOL,
    PENGAMBILAN_EMAS,
    PENJUALAN_EMAS,
    PENJUALAN_SEBAGIAN,
    UBAH_HARGA_KADAR,
    TAMBAH_KADAR,
    HAPUS_TRANSAKSI,
    UBAH_PENGATURAN,
    MANAJEMEN_PENGGUNA
}

enum class TimeFilter(val label: String) {
    TODAY("Hari ini"),
    THIS_WEEK("Minggu ini"),
    THIS_MONTH("Bulan ini"),
    THIS_YEAR("Tahun ini"),
    ALL_TIME("Semua"),
    CUSTOM("Custom")
}

data class DeductionRule(
    val type: DeductionType = DeductionType.PERCENT,
    val value: Double = 0.0
)

enum class DeductionType {
    PERCENT,
    NOMINAL,
    NONE
}

enum class ReportType(val title: String) {
    MODAL("Laporan Modal Usaha"),
    PENGAMBILAN_EMAS("Laporan Pengambilan Emas"),
    TROMOL("Laporan Tromol Pengolahan"),
    INVENTORY("Laporan Stok Inventory"),
    PENJUALAN("Laporan Penjualan Emas"),
    LABA_RUGI("Laporan Laba / Rugi"),
    ARUS_KAS("Laporan Arus Kas"),
    AKTIVITAS_USER("Laporan Log Aktivitas")
}
