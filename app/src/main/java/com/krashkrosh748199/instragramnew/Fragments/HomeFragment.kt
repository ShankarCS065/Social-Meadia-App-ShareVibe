package com.krashkrosh748199.instragramnew.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.krashkrosh748199.instragramnew.Adapter.PostAdapter
import com.krashkrosh748199.instragramnew.Model.Post
import com.krashkrosh748199.instragramnew.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private var postAdapter:PostAdapter?=null
    private var postList: MutableList<Post>?=null
    private var followingList: MutableList<Post>?=null
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        var recyclerView:RecyclerView?=null
        recyclerView=view.findViewById(R.id.recycler_view_home)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout=true
        linearLayoutManager.stackFromEnd=true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter=context?.let { PostAdapter(it,postList as ArrayList<Post>) }
        recyclerView.adapter = postAdapter

        checkFollowings()

        return view
    }

    private fun checkFollowings() {
        followingList = ArrayList()
        val followingRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Following")

        followingRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists()) {
                    (followingList as ArrayList<String>).clear()

                    for (snapshot in pO.children) {
                        snapshot.key?.let {
                            (followingList as ArrayList<String>).add(it)
                        }
                    }
                    retrievePosts()

                }
            }
            override fun onCancelled(pO: DatabaseError) {

            }
        })
    }

    private fun retrievePosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(pO: DataSnapshot) {

                postList?.clear()
                for(snapshot in pO.children){
                    val post = snapshot.getValue(Post::class.java)
                    for(id in (followingList as ArrayList<String>)){
                        if(post!!.getPublisher()==id){
                            postList!!.add(post)
                        }
                        postAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(pO: DatabaseError) {

            }
        })

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}