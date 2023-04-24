package com.upbetter.bt

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inuker.bluetooth.library.search.SearchResult
import com.upbetter.bt.bt.BtHelper

class DeviceAdapter(private val context: Context, private val data: List<SearchResult>) :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DeviceViewHolder, pos: Int) {
        holder.tvName.text = data[pos].name
        holder.tvMac.text = data[pos].address
        holder.tvRssi.text = "" + data[pos].rssi
        val isThisConnect =
            BtHelper.currentConnects.contains(data[pos])
        if (isThisConnect) {
            holder.btnConnect.setBackgroundResource(R.drawable.btn_red_bg)
            holder.btnConnect.text = "断开连接"
            holder.btnConnect.setOnClickListener {
                BtHelper.disconnect(data[pos])
            }
        } else {
            holder.btnConnect.setBackgroundResource(R.drawable.btn_enable_bg)
            holder.btnConnect.text = "连接"
            holder.btnConnect.setOnClickListener {
                BtHelper.connect(data[pos])
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_device, parent, false)
        )
    }

    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName = view.findViewById<TextView>(R.id.tv_device_name)
        val tvMac = view.findViewById<TextView>(R.id.tv_mac_address)
        val tvRssi = view.findViewById<TextView>(R.id.tv_rssi)
        val btnConnect = view.findViewById<Button>(R.id.btnConnect)
    }
}