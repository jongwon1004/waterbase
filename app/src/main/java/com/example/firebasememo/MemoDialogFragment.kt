package com.example.firebasememo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.firebasememo.databinding.DialogMemoBinding

// 優先度関連のインターフェース。優先度の選択やメモの更新をハンドルします。
interface MemoListener {
    fun onCreateMemo(memo: Memo)   // メモ作成が選択された時の処理
}

// 優先度の詳細を入力するためのダイアログフラグメント
class MemoDialogFragment : DialogFragment() {

    // ViewBindingのプロパティ
    private lateinit var binding: DialogMemoBinding  // 実際のバインディング変数

    // メモのイベントをハンドルするリスナー
    private var memoListener: MemoListener? = null

    // 定数
    companion object {
        const val TAG = "PiorityDialog"  // ログに表示するタグ
    }

    // ライフサイクルメソッド
    // Viewが作成されるときの処理
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ViewBindingを用いてViewを生成
        binding = DialogMemoBinding.inflate(inflater, container, false)
        setupClickListeners()
        return binding.root
    }

    // Fragmentがアタッチされるときの処理
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 親のフラグメントがPiorityListenerを実装しているか確認
        if (parentFragment is MemoListener) {
            memoListener = parentFragment as MemoListener
        }
    }

    // Fragmentが表示されるときの処理
    override fun onResume() {
        super.onResume()
        adjustDialogSize()  // ダイアログのサイズを調整
    }

    // UI関連のヘルパーメソッドとイベントハンドラー
    private fun setupClickListeners() {
        // Submitボタンがクリックされたときの処理を設定
        binding.memoFormButton.setOnClickListener { onSubmitClicked() }
        // キャンセルボタンがクリックされたときの処理を設定
        binding.memoFormCancel.setOnClickListener { onCancelClicked() }
    }

    // ダイアログのサイズを調整するメソッド
    private fun adjustDialogSize() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    // 送信ボタンがクリックされたときの処理
    private fun onSubmitClicked() {
        // 메모 텍스트와 우선도를 가져옴
        val text = binding.memoFormText.text.toString()
        val priority = String.format("%.1f", binding.memoFromPiority.rating).toDouble()

        // 메모 객체 생성
        val memo = Memo(text, priority)
        memoListener?.onCreateMemo(memo)  // 리스너를 통해 메모 생성
        dismiss()  // 다이얼로그 닫기
    }


    // キャンセルボタンがクリックされたときの処理
    private fun onCancelClicked() {
        dismiss()  // ダイアログを閉じる
    }


}
