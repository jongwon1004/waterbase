package com.example.firebasememo

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasememo.databinding.FragmentMainBinding

import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// メインのフラグメントクラスです。メモを表示、追加、更新する役割を持っています。
class MainFragment : Fragment(), MemoListener, OnMemoDeleteListener {
    // ... 클래스 구현 ...
    // プロパティの宣言部分
    // Firestoreのインスタンス
    private lateinit var firestore :FirebaseFirestore

    // Firestoreのクエリ
    private var query: Query? = null
    // ViewBindingのインスタンス
    private lateinit var binding: FragmentMainBinding
    // メモのアダプター
    private var adapter: MemoAdapter? = null

    // ログに表示するタグ
    companion object {
        private const val TAG = "MainFragment"
    }

    // Viewが作成されるときの処理
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // ViewBindingを用いてViewを生成
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Viewが作成された後の処理
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerViewのLayoutManagerを設定
        binding.recyclerMemos.layoutManager = LinearLayoutManager(context)

        // FirestoreとRecyclerViewの初期設定を行うメソッドを呼び出し
        initFirestore()

        // "memos"コレクションのFirestoreクエリを作成
        query = firestore.collection("memos")


        // Firestoreからメモを取得し、成功した場合はアダプターを設定
        (query as CollectionReference).get()
            .addOnSuccessListener {
                // 取得に成功した際の処理
                val documents = it.documents
                adapter = createAdapter(documents)
                binding.recyclerMemos.adapter = adapter
            }
            .addOnFailureListener() {
                Toast.makeText(context, "取得に失敗しました", Toast.LENGTH_SHORT).show()

            }


        // FAB（浮き出るアクションボタン）がクリックされたときに優先度ダイアログを表示するリスナーを設定
        binding.fabAddMemo.setOnClickListener { showMemoDialog() }
    }

    // Firestoreの初期設定を行うメソッド
    private fun initFirestore() {
        firestore = Firebase.firestore
        val query = firestore.collection("memos")
        query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            // 데이터가 변경되었을 때의 처리
            val documents = snapshots?.documents
            adapter = documents?.let { MemoAdapter(it, this) }
            binding.recyclerMemos.adapter = adapter
        }
    }


    // 新しいメモをFirestoreに追加するメソッド
    private fun addMemo(memo: Memo): Task<Void> = firestore
        .collection("memos")
        .document()
        .set(memo)



    // エラー時にSnackbarでメッセージを表示するメソッド
    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, "エラー: $message", Snackbar.LENGTH_LONG).show()
    }

    // メモダイアログを表示するメソッド
    private fun showMemoDialog() {
        val memoDialog = MemoDialogFragment()
        memoDialog.show(childFragmentManager, MemoDialogFragment.TAG)
    }

    // キーボードを隠すメソッド
    private fun hideKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // 優先度が選択されたときのコールバックメソッド
    override fun onCreateMemo(memo: Memo) {
        // メモのデータをFireStoreのmemosコレクションに追加する処理が必要
        addMemo(memo).addOnSuccessListener {
            // メモの追加に成功した際の処理
            Toast.makeText(context, "メモの追加に成功しました", Toast.LENGTH_SHORT).show()
        }
            .addOnFailureListener {
                Toast.makeText(context,"メモの追加に失敗しました", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDeleteMemo(documentId: String) {
        // 삭제 로직
        firestore.collection("memos").document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "メモを削除しました", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "メモの削除に失敗しました", Toast.LENGTH_SHORT).show()
            }
    }





    // Firestoreのドキュメントからアダプターを作成するメソッド
    private fun createAdapter(documents: List<DocumentSnapshot>): MemoAdapter {
        return MemoAdapter(documents, this)
    }

}
